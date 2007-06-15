#ifdef WIN32

#define _WIN32_WINNT 0x0500
#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <fcntl.h>
#include <io.h>
#define UNUSED 0xDEADBEEF
#define LOG_DAEMON UNUSED

void syslog(int pri, const TCHAR * msg);

#else

#include <unistd.h>             /* fork(), execl(), readlink(), chdir(), ... */
#include <sys/types.h>          /* pid_t */
#include <sys/wait.h>           /* WEXITSTATUS(), waitpid() */
#include <sys/stat.h>           /* umask() */
#include <syslog.h>
#include <stdlib.h>             /* system(), exit(), EXIT_... */
#include <string.h>             /* strrchr() */
#include <fcntl.h>              /* open(), O_RDWR */
#include <limits.h>             /* PATH_MAX */
#include <signal.h>             /* signal(), kill(), raise(), SIG... */
#include <stdio.h>              /* FILE, fgets(), fdopen() */
#include <pwd.h>                /* getpwnam() */

static int nfs_delay;

#endif

#include <time.h>

#include "proactivep2p.h"

/*
* If the jvm crashes in less than one minute, three times, there is a problem,
* we shut down the daemon. This is disabled for now, because there is a delay
* during the restart, and every crashing events we encountered would disappear
* after some delay.
*/
#define ACCEPTABLE_CRASH_DELAY 60
#define MAX_CRASH_COUNT 3

typedef enum {
    DAEMON_TIME_ERROR,
    DAEMON_JVM_KEEPS_CRASHING,
    DAEMON_JVM_EXIT_FATAL,
    DAEMON_WRONG_PARAM,
    DAEMON_UNKNOWN_ERROR,
} daemon_ret_t;

/*
* Exit codes :   0 => OK, restart the daemon
*              220 => Error, don't restart
*                2 => Restart but wait for the next period
*            other => Error, restart
*
* These exit codes must be synchronized with the Java code
*/
#define EXIT_OK 0
#define EXIT_FATAL 220
#define EXIT_NEXT_RUN 2


#ifdef WIN32

static void daemonize(void)
{
}

static void init_random_sleep(void)
{
    /* TODO */
}

static void random_sleep(int nb_seconds
{
    /* TODO */
}

static void symbolink_link_cd(const TCHAR * filename)
{
    TCHAR buffer[MAX_PATH];
    FILE *file;

    file = _tfopen(filename, TEXT("rt"));
    if (file == NULL)
        return;

    if (_fgetts(buffer, sizeof(buffer) / sizeof(TCHAR), file) != NULL) {
        SetCurrentDirectory(buffer);
    }

    fclose(file);
}

static void change_to_daemon_dir(void)
{
    TCHAR szDllName[_MAX_PATH];
    TCHAR szApp[_MAX_PATH * 2];
    int i, len;

    GetModuleFileName(0, szDllName, _MAX_PATH);
    len = wcslen(szDllName);

    wcscpy(szApp, szDllName);
    for (i = len - 1; i > 0; i--) {
        if (szApp[i] == '\\') {
            szApp[i + 1] = 0;
            break;
        }
    }

    SetCurrentDirectory(szApp);
    symbolink_link_cd(DAEMON_DIR);
}

static PROCESS_INFORMATION piProcInfo;
extern TCHAR *user_name;
extern TCHAR *domain;
extern TCHAR *password;

static BOOL logged = FALSE;
static HANDLE user_token;

static int execute(const TCHAR * cmd, const TCHAR * flag)
{
    SECURITY_ATTRIBUTES saAttr;
    BOOL fSuccess;
    HANDLE hChildStdoutRd, hChildStdoutWr, hChildStdoutRdDup, hStdout;

    // Set the bInheritHandle flag so pipe handles are inherited. 
    saAttr.nLength = sizeof(SECURITY_ATTRIBUTES);
    saAttr.bInheritHandle = TRUE;
    saAttr.lpSecurityDescriptor = NULL;

    // Get the handle to the current STDOUT. 
    hStdout = GetStdHandle(STD_OUTPUT_HANDLE);

    // Create a pipe for the child process's STDOUT. 
    if (!CreatePipe(&hChildStdoutRd, &hChildStdoutWr, &saAttr, 0))
        return EXIT_FATAL;

    // Create noninheritable read handle and close the inheritable read 
    // handle. 
    fSuccess =
        DuplicateHandle(GetCurrentProcess(), hChildStdoutRd,
                        GetCurrentProcess(), &hChildStdoutRdDup, 0, FALSE,
                        DUPLICATE_SAME_ACCESS);
    if (!fSuccess)
        return EXIT_FATAL;
    CloseHandle(hChildStdoutRd);

    // Now create the child process. 
    STARTUPINFO siStartInfo;
    BOOL bFuncRetn = FALSE;

    // Set up members of the PROCESS_INFORMATION structure. 
    ZeroMemory(&piProcInfo, sizeof(PROCESS_INFORMATION));

    // Set up members of the STARTUPINFO structure. 
    ZeroMemory(&siStartInfo, sizeof(STARTUPINFO));
    siStartInfo.cb = sizeof(STARTUPINFO);
    siStartInfo.hStdError = hChildStdoutWr;
    siStartInfo.hStdOutput = hChildStdoutWr;
    siStartInfo.dwFlags = STARTF_USESTDHANDLES;

    TCHAR *cmd_dup;

    if (flag == NULL) {
        cmd_dup = _tcsdup(cmd);
    } else {
        cmd_dup =
            (TCHAR *) malloc((_tcslen(cmd) + 2 + _tcslen(flag)) *
                             sizeof(TCHAR));
        _stprintf(cmd_dup, TEXT("%s %s"), cmd, flag);
    }

    // Create the child process.
    if (user_name != NULL) {
        if (!logged) {
            logged = LogonUser(user_name,       // lpszUsername,
                               domain,  // lpszDomain,
                               password,        // lpszPassword,
                               LOGON32_LOGON_INTERACTIVE,       // dwLogonType,
                               LOGON32_PROVIDER_DEFAULT,        // dwLogonProvider,
                               &user_token);    // phToken
            if (!logged) {
                Error();
                return EXIT_FATAL;
            }
        }

        bFuncRetn = CreateProcessAsUser(user_token,     // user token
                                        NULL,   // application name
                                        cmd_dup,        // command line 
                                        NULL,   // process security attributes 
                                        NULL,   // primary thread security attributes 
                                        TRUE,   // handles are inherited
                                        IDLE_PRIORITY_CLASS,    // creation flags 
                                        NULL,   // use parent's environment 
                                        NULL,   // use parent's current directory 
                                        &siStartInfo,   // STARTUPINFO pointer 
                                        &piProcInfo);   // receives PROCESS_INFORMATION 
    } else
        bFuncRetn = CreateProcess(NULL, // application name
                                  cmd_dup,      // command line 
                                  NULL, // process security attributes 
                                  NULL, // primary thread security attributes 
                                  TRUE, // handles are inherited
                                  IDLE_PRIORITY_CLASS,  // creation flags 
                                  NULL, // use parent's environment 
                                  NULL, // use parent's current directory 
                                  &siStartInfo, // STARTUPINFO pointer 
                                  &piProcInfo); // receives PROCESS_INFORMATION 
    if (bFuncRetn == 0) {
        syslog(LOG_INFO, cmd_dup);
        Error();
        syslog(LOG_INFO, cmd_dup);
        return EXIT_FATAL;
    }

    free(cmd_dup);
    CloseHandle(piProcInfo.hThread);

    // Close the write end of the pipe before reading from the 
    // read end of the pipe. 
    CloseHandle(hChildStdoutWr);

    // Read output from the child process. 
    int fd = _open_osfhandle((long) hChildStdoutRdDup, _O_RDONLY | _O_TEXT);
    FILE *child_stdout = _tfdopen(fd, TEXT("rt"));
    TCHAR buffer[1024];
    int prefix_length = _tcslen(DAEMON_LOG);
    while (_fgetts(buffer, sizeof(buffer) / sizeof(TCHAR), child_stdout) !=
           NULL) {
        if (!_tcsncmp(DAEMON_LOG, buffer, prefix_length))
            syslog(LOG_INFO, buffer + prefix_length);
    }
    fclose(child_stdout);

    DWORD ret;
    GetExitCodeProcess(piProcInfo.hProcess, &ret);

    CloseHandle(piProcInfo.hProcess);

    return ret;
}

static void openlog(TCHAR * ident, int logstat, int logfac)
{
}

static void closelog()
{
}

#else

static pid_t child_pid;

static int change_uid(const char *username)
{
    struct passwd *passwd;

    passwd = getpwnam(username);
    if (passwd == NULL)
        return -1;

    return setuid(passwd->pw_uid);
}

static void drop_privileges(void)
{
    if (change_uid(DAEMON_USER) < 0)
        change_uid("nobody");
}

static void forward_signal(int sig)
{
    if (child_pid != 0)
        kill(child_pid, sig);

    signal(sig, SIG_DFL);
    raise(sig);
}

static void init_random_sleep(void)
{
    srandom(time(NULL) ^ getpid());
}

static void random_sleep(int nb_seconds)
{
    if (nfs_delay) {
        struct timespec sleep_time;

	sleep_time.tv_nsec = random() % 1000000000;
	sleep_time.tv_sec = random() % nb_seconds;

	nanosleep(&sleep_time, NULL);
    }
}


static void daemonize(void)
{
    pid_t pid;
    int fd;

    if (getppid() == 1)
        return;                 /* already a daemon */

    pid = fork();
    if (pid < 0)
        exit(EXIT_FAILURE);

    if (pid > 0)
        exit(EXIT_SUCCESS);

    /* child (daemon) continues */
    setsid();                   /* obtain a new process group */

    umask(027);                 /* set newly created file permissions */

    for (fd = 0; fd < 3; fd++)
        close(fd);              /* close all descriptors */

    fd = open("/dev/null", O_RDWR);     /* STDIN */
    if (fd < 0)
        return;

    dup(fd);                    /* STDOUT */
    dup(fd);                    /* STDERR */
}

static void change_to_daemon_dir(void)
{
    char buffer[PATH_MAX];
    int length;
    char *last_slash;

    length = readlink("/proc/self/exe", buffer, sizeof(buffer));
    if (length < 0 || length >= sizeof(buffer))
        return;

    buffer[length] = '\0';
    last_slash = strrchr(buffer, '/');
    if (last_slash != NULL)
        *last_slash = '\0';

    chdir(buffer);
    chdir("..");
    chdir(DAEMON_DIR);
}

static int execute(const char *cmd, const char *flag)
{
    pid_t pid;
    int fd[2];

    if (pipe(fd) < 0)
        return EXIT_FATAL;

    pid = fork();
    if (pid < 0) {
        close(fd[0]);
        close(fd[1]);
        return EXIT_FATAL;
    }

    if (pid == 0) {
        /* Child */
	char *args[4];

        close(fd[0]);
        close(STDOUT_FILENO);
        dup2(fd[1], STDOUT_FILENO);

	args[0] = (char*) cmd;
	args[1] = (char*) (flag ? flag : "");
	args[2] = (char*) (nfs_delay ? "-delay" : "");
	args[3] = NULL;

	execv(cmd, args);

        exit(EXIT_FAILURE);
    }

    {
        /* Parent */
        int status;
        pid_t wait_pid;
        char buffer[1024];
        FILE *java_out;
        int prefix_length = strlen(DAEMON_LOG);

        child_pid = pid;
        signal(SIGHUP, forward_signal);
        signal(SIGINT, forward_signal);
        signal(SIGQUIT, forward_signal);
        signal(SIGTERM, forward_signal);

        close(fd[1]);
        java_out = fdopen(fd[0], "r");
        if (java_out == NULL) {
            kill(child_pid, SIGKILL);
            close(fd[0]);
            return EXIT_FATAL;
        }

        while (fgets(buffer, sizeof(buffer), java_out) != NULL) {
            if (!strncmp(DAEMON_LOG, buffer, prefix_length)) {
                syslog(LOG_INFO, buffer + prefix_length);
            }
        }

        wait_pid = waitpid(pid, &status, 0);
        close(fd[0]);
        if (wait_pid < 0)
            return EXIT_FATAL;

        return WEXITSTATUS(status);
    }
}

#endif

static daemon_ret_t run_daemon(const TCHAR * cmd)
{
    time_t start, end;
    int nb_crash = 0;
    int java_ret;
    const TCHAR *flag = NULL;

    change_to_daemon_dir();
    init_random_sleep();

    for (;;) {
        random_sleep(120);

        start = time(NULL);
        if (start < 0) {
            return DAEMON_TIME_ERROR;
        }

        java_ret = execute(cmd, flag);
        syslog(LOG_INFO, TEXT("JVM stopped"));

        switch (java_ret) {
        case EXIT_OK:
            flag = NULL;
            continue;

        case EXIT_FATAL:
            return DAEMON_JVM_EXIT_FATAL;

        case EXIT_NEXT_RUN:
            flag = TEXT("-n");
            continue;

        default:
            flag = NULL;
            /* Record the timestamp, restart */
            break;
        }

	/* The limitation on the number of restarts is disabled now */
	random_sleep(120);
	continue;
        end = time(NULL);
        if (end < 0) {
            return DAEMON_TIME_ERROR;
        }

        if (end - start < ACCEPTABLE_CRASH_DELAY) {
            nb_crash++;
            if (nb_crash >= MAX_CRASH_COUNT) {
                return DAEMON_JVM_KEEPS_CRASHING;
            }
        } else
            nb_crash = 0;
    }

    return DAEMON_UNKNOWN_ERROR;
}

static void init_logging(void)
{
    openlog(DAEMON_NAME, 0, LOG_DAEMON);
}

void p2p_daemon(int argc, TCHAR * argv[])
{
    daemon_ret_t ret;

    init_logging();

    ret = run_daemon(DEFAULT_PRG);

    switch (ret) {
    case DAEMON_TIME_ERROR:
        syslog(LOG_NOTICE,
               TEXT("Unknown problem with the time() function, leaving"));
        break;

    case DAEMON_JVM_KEEPS_CRASHING:
        syslog(LOG_NOTICE, TEXT("The JVM keeps crashing, leaving"));
        break;

    case DAEMON_JVM_EXIT_FATAL:
        syslog(LOG_NOTICE, TEXT("The JVM exited, leaving"));
        break;

    case DAEMON_WRONG_PARAM:
        syslog(LOG_NOTICE, TEXT("Wrong number of parameters, leaving"));
        break;

    default:
        syslog(LOG_NOTICE, TEXT("Unknown error, leaving"));
        break;
    }

    closelog();
}

#ifndef WIN32

static void set_nfs_delay(int argc, char *argv[])
{
    int i;

    for (i = 0; i < argc; i++)
      if (!strcmp(argv[i], "-delay"))
	nfs_delay = 1;
}

int main(int argc, char *argv[])
{
    change_to_daemon_dir();
    drop_privileges();
    daemonize();
    set_nfs_delay(argc, argv);
    p2p_daemon(argc, argv);
    return EXIT_FAILURE;
}

#endif
