#ifdef WIN32
/* ProActive P2P Daemon based on the Subversion Win32 Service wrapper */
/*
SVNService modifications written by Magnus Norddahl.

Disclaimer: Yes this code is so ugly (that I added, not the original Craig Link stuff)
that it may burn down your PC and kill your cat! But what can I say.. its C, and all
I got is the platform SDK here. Bit violations of TCHAR stuff (dont use the _TEXT things
always, some hardcoded widechar) and some strings should be defined in header file.

*/
// THIS CODE AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY OF
// ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO
// THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
// PARTICULAR PURPOSE.
//
// Copyright (C) 1993-1997  Microsoft Corporation.  All Rights Reserved.
//
//  MODULE:   service.c
//
//  PURPOSE:  Implements functions required by all services
//            windows.
//
//  FUNCTIONS:
//    main(int argc, char **argv);
//    service_ctrl(DWORD dwCtrlCode);
//    service_main(DWORD dwArgc, LPTSTR *lpszArgv);
//    CmdInstallService();
//    CmdRemoveService();
//    CmdDebugService(int argc, TCHAR **argv);
//    ControlHandler ( DWORD dwCtrlType );
//    GetLastErrorText( LPTSTR lpszBuf, DWORD dwSize );
//
//  COMMENTS:
//
//  AUTHOR: Craig Link - Microsoft Developer Support
//

#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <time.h>

#include "proactivep2p.h"
#include "XEventLog.h"

// internal variables
static SERVICE_STATUS ssStatus; // current status of the service
static SERVICE_STATUS_HANDLE sshStatusHandle;
static DWORD dwErr = 0;
static BOOL bDebug = FALSE;
static TCHAR szErr[256];

// internal function prototypes
static VOID WINAPI service_ctrl(DWORD dwCtrlCode);
static VOID WINAPI service_main(DWORD dwArgc, LPTSTR * lpszArgv);
static VOID CmdInstallService(void);
static VOID CmdRemoveService();
static VOID CmdDebugService(int argc, TCHAR ** argv);
static BOOL WINAPI ControlHandler(DWORD dwCtrlType);
static LPTSTR GetLastErrorText(LPTSTR lpszBuf, DWORD dwSize);
static VOID AddToMessageLog(const TCHAR * lpszMsg, int error_type);
static VOID ServiceStart(DWORD dwArgc, LPTSTR * lpszArgv);
static VOID ServiceStop();
static BOOL ReportStatusToSCMgr(DWORD dwCurrentState, DWORD dwWin32ExitCode,
                                DWORD dwWaitHint);

TCHAR *user_name;
TCHAR *domain;
TCHAR *password;

//
//  FUNCTION: main
//
//  PURPOSE: entrypoint for service
//
//  PARAMETERS:
//    argc - number of command line arguments
//    argv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    main() either performs the command line task, or
//    call StartServiceCtrlDispatcher to register the
//    main service thread.  When the this call returns,
//    the service has stopped, so exit.
//
int _tmain(int argc, TCHAR ** argv)
{
    SERVICE_TABLE_ENTRY dispatchTable[] = {
        {DAEMON_NAME, (LPSERVICE_MAIN_FUNCTION) service_main},
        {NULL, NULL}
    };

    if (argc > 4) {
        user_name = argv[2];
        domain = argv[3];
        password = argv[4];
    }

    if (argc > 1 && (*argv[1] == '-' || *argv[1] == '/')) {
        if (_tcsicmp(TEXT("install"), argv[1] + 1) == 0)
            CmdInstallService();

        else if (_tcsicmp(TEXT("remove"), argv[1] + 1) == 0)
            CmdRemoveService();

        else if (_tcsicmp(TEXT("debug"), argv[1] + 1) == 0) {
            bDebug = TRUE;
            CmdDebugService(1, argv);
        } else
            goto dispatch;
        return 0;
    }
    // if it doesn't match any of the above parameters
    // the service control manager may be starting the service
    // so we must call StartServiceCtrlDispatcher
  dispatch:
    // this is just to be friendly
    _tprintf(TEXT("%s -install       to install the service\n"), argv[0]);
    _tprintf(TEXT("%s -remove        to remove the service\n"), argv[0]);
    _tprintf(TEXT("%s -debug         to run as a console app for debugging\n"),
             argv[0]);
    _tprintf(TEXT("\nStartServiceCtrlDispatcher being called.\n"));
    _tprintf(TEXT("This may take several seconds.  Please wait.\n"));

    if (!StartServiceCtrlDispatcher(dispatchTable))
        AddToMessageLog(TEXT("StartServiceCtrlDispatcher failed."),
                        EVENTLOG_WARNING_TYPE);

    return 0;
}


//
//  FUNCTION: service_main
//
//  PURPOSE: To perform actual initialization of the service
//
//  PARAMETERS:
//    dwArgc   - number of command line arguments
//    lpszArgv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    This routine performs the service initialization and then calls
//    the user defined ServiceStart() routine to perform majority
//    of the work.
//
static void WINAPI service_main(DWORD dwArgc, LPTSTR * lpszArgv)
{
    if (lpszArgv != NULL && dwArgc > 4) {
        user_name = lpszArgv[2];
        domain = lpszArgv[3];
        password = lpszArgv[4];
    }
    // register our service control handler:
    //
    sshStatusHandle = RegisterServiceCtrlHandler(DAEMON_NAME, service_ctrl);
    if (!sshStatusHandle)
        goto cleanup;

    // SERVICE_STATUS members that don't change in example
    //
    ssStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
    ssStatus.dwServiceSpecificExitCode = 0;

    // report the status to the service control manager.
    //
    if (ReportStatusToSCMgr(SERVICE_START_PENDING, NO_ERROR, 3000)) {
        ServiceStart(dwArgc, lpszArgv);
    }
  cleanup:
    // try to report the stopped status to the service control manager.
    //
    if (sshStatusHandle)
        ReportStatusToSCMgr(SERVICE_STOPPED, dwErr, 0);
}


//
//  FUNCTION: service_ctrl
//
//  PURPOSE: This function is called by the SCM whenever
//           ControlService() is called on this service.
//
//  PARAMETERS:
//    dwCtrlCode - type of control requested
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
static VOID WINAPI service_ctrl(DWORD dwCtrlCode)
{
    // Handle the requested control code.
    //
    switch (dwCtrlCode) {

        // Stop the service.
        //
        // SERVICE_STOP_PENDING should be reported before
        // setting the Stop Event - hServerStopEvent - in
        // ServiceStop().  This avoids a race condition
        // which may result in a 1053 - The Service did not respond...
        // error.
    case SERVICE_CONTROL_STOP:
        ReportStatusToSCMgr(SERVICE_STOP_PENDING, NO_ERROR, 0);
        ServiceStop();
        return;

        // Update the service status.
        //
    case SERVICE_CONTROL_INTERROGATE:
        break;

        // invalid control code
        //
    default:
        break;
    }
    ReportStatusToSCMgr(ssStatus.dwCurrentState, NO_ERROR, 0);
}


//
//  FUNCTION: ReportStatusToSCMgr()
//
//  PURPOSE: Sets the current status of the service and
//           reports it to the Service Control Manager
//
//  PARAMETERS:
//    dwCurrentState - the state of the service
//    dwWin32ExitCode - error code to report
//    dwWaitHint - worst case estimate to next checkpoint
//
//  RETURN VALUE:
//    TRUE  - success
//    FALSE - failure
//
//  COMMENTS:
//
BOOL ReportStatusToSCMgr(DWORD dwCurrentState, DWORD dwWin32ExitCode,
                         DWORD dwWaitHint)
{
    static DWORD dwCheckPoint = 1;
    BOOL fResult = TRUE;
    if (!bDebug)                // when debugging we don't report to the SCM
    {
        if (dwCurrentState == SERVICE_START_PENDING)
            ssStatus.dwControlsAccepted = 0;

        else
            ssStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;
        ssStatus.dwCurrentState = dwCurrentState;
        ssStatus.dwWin32ExitCode = dwWin32ExitCode;
        ssStatus.dwWaitHint = dwWaitHint;
        if ((dwCurrentState == SERVICE_RUNNING) ||
            (dwCurrentState == SERVICE_STOPPED))
            ssStatus.dwCheckPoint = 0;

        else
            ssStatus.dwCheckPoint = dwCheckPoint++;

        // Report the status of the service to the service control manager.
        //
        if (!(fResult = SetServiceStatus(sshStatusHandle, &ssStatus))) {
            Error();
            AddToMessageLog(TEXT("SetServiceStatus failed"),
                            EVENTLOG_WARNING_TYPE);
        }
    }
    return fResult;
}


// Debug level defaults at zero:
static int debug_level = 0;
static FILE *debug_file = NULL;

//
//  FUNCTION: AddToMessageLog(int eventID, LPCTSTR lpszMsg, int error_type)
//
//  PURPOSE: Allows any thread to log an error message
//
//  PARAMETERS:
//    eventID - Event ID.
//    lpszMsg - text for message
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
static TCHAR buffer[1024];
static XEventLog *el = NULL;
static VOID AddToMessageLog(const TCHAR * lpszMsg, int error_type)
{
    __time64_t ltime;
    TCHAR date[128];
    int i, len;
    if (debug_file != NULL) {
        _time64(&ltime);
        _tcscpy(date, _tctime64(&ltime));
        date[_tcslen(date) - 1] = 0;
        _stprintf(buffer, TEXT("%s: %s\n"), date, lpszMsg);
        len = _tcslen(buffer);
        for (i = 0; i < len - 1; i++) {
            if (buffer[i] == '\n')
                buffer[i] = '\t';
            if (buffer[i] == '\r')
                buffer[i] = '\n';
        }
        _fputts(buffer, debug_file);
        fflush(debug_file);
    }
    if (!bDebug) {
        if (el == NULL) {
            el = new XEventLog();
        }
        el->Init(DAEMON_NAME);
        if (!el->Write(error_type, (LPCTSTR) lpszMsg))
            _tprintf(TEXT("Log problem\n"));
        el->Close();
    } else {
        _tprintf(TEXT("MessageLog: %s\n"), lpszMsg);
    }
}


//
//  FUNCTION: AddDebug(int debuglevel, LPTSTR lpszMsg)
//
//  PURPOSE: Write debug information if the error level allows it.
//
//  PARAMETERS:
//    debuglevel - level this debug information belong to.
//    lpszMsg - text for message
//
//  RETURN VALUE:
//    none
//
static void _AddDebug(int level, LPTSTR lpszMsg)
{
    if (level > debug_level)
        return;
    AddToMessageLog(lpszMsg, EVENTLOG_INFORMATION_TYPE);
}


//////////////////////////////////////////////////////////////////////////////

//
//  FUNCTION: SetDebugLevel(int debuglevel)
//
//  PURPOSE: Changes the debug level at which things are written to the event log.
//
//  PARAMETERS:
//    debuglevel - New debug level to use.
//
//  RETURN VALUE:
//    none
//
static void SetDebugLevel(int new_level)
{
    debug_level = new_level;
}

//////////////////////////////////////////////////////////////////////////////

//
//  FUNCTION: LogToFile(const char *filename)
//
//  PURPOSE: Changes all logging to be sent to file.
//
//  PARAMETERS:
//    filename - Log filename.
//
//  RETURN VALUE:
//    none
//
static void LogToFile(const TCHAR * filename)
{
    if (debug_file != NULL)
        fclose(debug_file);
    debug_file = _tfopen(filename, TEXT("a+t"));
    if (debug_file == NULL)
        debug_file = _tfopen(filename, TEXT("wt"));
}


//////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////
//
//  The following code handles service installation and removal
//

//
//  FUNCTION: CmdInstallService()
//
//  PURPOSE: Installs the service
//
//  PARAMETERS:
//    none
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
static void CmdInstallService(void)
{
    SC_HANDLE schService;
    SC_HANDLE schSCManager;
    TCHAR szPath[512];
    TCHAR cmd[2048];
    HKEY log_key;
    LONG err;

    if (GetModuleFileName(NULL, szPath, 512) == 0) {
        _tprintf(TEXT("Unable to install %s - %s\n"), DAEMON_NAME,
                 GetLastErrorText(szErr, 256));
        return;
    }

    if (user_name != NULL)
        _stprintf(cmd, TEXT("%s nothing %s %s %s"), szPath, user_name, domain,
                  password);
    else
        _stprintf(cmd, TEXT("%s"), szPath);

    schSCManager = OpenSCManager(NULL,  // machine (NULL == local)
                                 NULL,  // database (NULL == default)
                                 SC_MANAGER_ALL_ACCESS  // access required
        );
    if (schSCManager) {
        schService = CreateService(schSCManager,        // SCManager database
                                   DAEMON_NAME, // name of service
                                   DAEMON_NAME, // name to display
                                   SERVICE_ALL_ACCESS,  // desired access
                                   SERVICE_WIN32_OWN_PROCESS,   // service type
                                   SERVICE_AUTO_START,  // start type
                                   SERVICE_ERROR_IGNORE,        // error control type
                                   cmd, // service's binary
                                   NULL,        // no load ordering group
                                   NULL,        // no tag identifier
                                   NULL,        // no dependencies
                                   NULL,        // LocalSystem account
                                   NULL);       // no password
        if (schService) {
            _tprintf(TEXT("%s installed.\n"), DAEMON_NAME);
            StartService(schService, 0, NULL);
            CloseServiceHandle(schService);
        } else {
            _tprintf(TEXT("CreateService failed - %s\n"),
                     GetLastErrorText(szErr, 256));
        }
        CloseServiceHandle(schSCManager);
    } else
        _tprintf(TEXT("OpenSCManager failed - %s\n"),
                 GetLastErrorText(szErr, 256));

    // Try to register the log message file:
    err = RegCreateKeyEx(HKEY_LOCAL_MACHINE, TEXT
                         ("SYSTEM\\CurrentControlSet\\Services\\EventLog\\Application\\P2PDaemon"),
                         0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS,
                         NULL, &log_key, NULL);
    if (err != ERROR_SUCCESS)
        _tprintf(TEXT("Could not register log message file.\n"));
    err = RegSetValueEx(log_key, TEXT("EventMessageFile"), 0, REG_SZ,
                        (LPBYTE) szPath, (wcslen(szPath) + 1) * sizeof(TCHAR));
    if (err != ERROR_SUCCESS)
        _tprintf(TEXT("Could not set message file.\n"));
    err = RegCloseKey(log_key);
    if (err != ERROR_SUCCESS)
        _tprintf(TEXT("Could not close reg key.\n"));
}


//
//  FUNCTION: CmdRemoveService()
//
//  PURPOSE: Stops and removes the service
//
//  PARAMETERS:
//    none
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
static void CmdRemoveService()
{
    SC_HANDLE schService;
    SC_HANDLE schSCManager;
    schSCManager = OpenSCManager(NULL,  // machine (NULL == local)
                                 NULL,  // database (NULL == default)
                                 SC_MANAGER_ALL_ACCESS  // access required
        );
    if (schSCManager) {
        schService = OpenService(schSCManager, DAEMON_NAME, SERVICE_ALL_ACCESS);
        if (schService) {

            // try to stop the service
            if (ControlService(schService, SERVICE_CONTROL_STOP, &ssStatus)) {
                _tprintf(TEXT("Stopping %s\n"), DAEMON_NAME);
                Sleep(1000);
                while (QueryServiceStatus(schService, &ssStatus)) {
                    if (ssStatus.dwCurrentState == SERVICE_STOP_PENDING) {
                        _tprintf(TEXT("."));
                        Sleep(1000);
                    } else
                        break;
                }
                if (ssStatus.dwCurrentState == SERVICE_STOPPED)
                    _tprintf(TEXT("\n%s stopped.\n"), DAEMON_NAME);

                else
                    _tprintf(TEXT("\n%s failed to stop.\n"), DAEMON_NAME);
            }
            // now remove the service
            if (DeleteService(schService))
                _tprintf(TEXT("%s removed.\n"), DAEMON_NAME);

            else
                _tprintf(TEXT("DeleteService failed - %s\n"),
                         GetLastErrorText(szErr, 256));
            CloseServiceHandle(schService);
        } else
            _tprintf(TEXT("OpenService failed - %s\n"),
                     GetLastErrorText(szErr, 256));
        CloseServiceHandle(schSCManager);
    } else
        _tprintf(TEXT("OpenSCManager failed - %s\n"),
                 GetLastErrorText(szErr, 256));
}


///////////////////////////////////////////////////////////////////
//
//  The following code is for running the service as a console app
//

//
//  FUNCTION: CmdDebugService(int argc, TCHAR ** argv)
//
//  PURPOSE: Runs the service as a console application
//
//  PARAMETERS:
//    argc - number of command line arguments
//    argv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
static void CmdDebugService(int argc, TCHAR ** argv)
{
    _tprintf(TEXT("Debugging %s.\n"), DAEMON_NAME);
    LogToFile(TEXT("log.txt"));
    SetConsoleCtrlHandler(ControlHandler, TRUE);
    ServiceStart(argc, argv);
}

//
//  FUNCTION: ControlHandler ( DWORD dwCtrlType )
//
//  PURPOSE: Handled console control events
//
//  PARAMETERS:
//    dwCtrlType - type of control event
//
//  RETURN VALUE:
//    True - handled
//    False - unhandled
//
//  COMMENTS:
//
static BOOL WINAPI ControlHandler(DWORD dwCtrlType)
{
    switch (dwCtrlType) {
    case CTRL_BREAK_EVENT:     // use Ctrl+C or Ctrl+Break to simulate
    case CTRL_C_EVENT:         // SERVICE_CONTROL_STOP in debug mode
        _tprintf(TEXT("Stopping %s.\n"), DAEMON_NAME);
        ServiceStop();
        return TRUE;
        break;
    }
    return FALSE;
}


//
//  FUNCTION: GetLastErrorText
//
//  PURPOSE: copies error message text to string
//
//  PARAMETERS:
//    lpszBuf - destination buffer
//    dwSize - size of buffer
//
//  RETURN VALUE:
//    destination buffer
//
//  COMMENTS:
//
static LPTSTR GetLastErrorText(LPTSTR lpszBuf, DWORD dwSize)
{
    DWORD dwRet;
    LPTSTR lpszTemp = NULL;
    dwRet =
        FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
                      FORMAT_MESSAGE_FROM_SYSTEM |
                      FORMAT_MESSAGE_ARGUMENT_ARRAY, NULL, GetLastError(),
                      LANG_NEUTRAL, (LPTSTR) & lpszTemp, 0, NULL);

    // supplied buffer is not long enough
    if (!dwRet || ((long) dwSize < (long) dwRet + 14))
        lpszBuf[0] = TEXT('\0');

    else {
        lpszTemp[lstrlen(lpszTemp) - 2] = TEXT('\0');   //remove cr and newline character
        _stprintf(lpszBuf, TEXT("%s (0x%x)"), lpszTemp, GetLastError());
    }
    if (lpszTemp)
        LocalFree((HLOCAL) lpszTemp);
    return lpszBuf;
}
static VOID ServiceStart(DWORD dwArgc, LPTSTR * lpszArgv)
{
    ReportStatusToSCMgr(SERVICE_START_PENDING, NO_ERROR, 5000);
    ReportStatusToSCMgr(SERVICE_RUNNING, NO_ERROR, 0);
    p2p_daemon(dwArgc, lpszArgv);
    ReportStatusToSCMgr(SERVICE_STOP_PENDING, NO_ERROR, 30000);
    ReportStatusToSCMgr(SERVICE_STOPPED, NO_ERROR, 1000);
}

static VOID ServiceStop()
{
    ReportStatusToSCMgr(SERVICE_STOP_PENDING, NO_ERROR, 2000);
    SOCKET lhSocket;
    SOCKADDR_IN lSockAddr;
    WSADATA wsaData;
    int lConnect;
    if (WSAStartup(MAKEWORD(2, 0), &wsaData) != 0)
        return;
    lhSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (lhSocket == INVALID_SOCKET)
        return;
    memset(&lSockAddr, 0, sizeof(lSockAddr));
    lSockAddr.sin_family = AF_INET;
    lSockAddr.sin_port = htons(DAEMON_PORT);
    lSockAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    lConnect = connect(lhSocket, (SOCKADDR *) & lSockAddr, sizeof(SOCKADDR_IN));
    if (lConnect != 0)
        return;
    send(lhSocket, KILL_CMD, strlen(KILL_CMD), 0);
    closesocket(lhSocket);
    ReportStatusToSCMgr(SERVICE_STOPPED, NO_ERROR, 0);
}

void syslog(int pri, const TCHAR * msg)
{
    int error_type;
    switch (pri) {
    case LOG_INFO:
        error_type = EVENTLOG_INFORMATION_TYPE;
        break;
    case LOG_NOTICE:
    default:
        error_type = EVENTLOG_WARNING_TYPE;
        break;
    }
    AddToMessageLog(msg, error_type);
}

void Error(void)
{
    LPVOID lpMsgBuf;
    DWORD dw = GetLastError();

    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
                  FORMAT_MESSAGE_FROM_SYSTEM,
                  NULL,
                  dw,
                  MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                  (LPTSTR) & lpMsgBuf, 0, NULL);

    _tprintf(TEXT("error %d: %s\n"), dw, lpMsgBuf);
    syslog(LOG_NOTICE, (TCHAR *) lpMsgBuf);

    LocalFree(lpMsgBuf);
}

#endif
