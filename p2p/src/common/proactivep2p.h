#ifndef DAEMON_H
#define DAEMON_H

#ifndef WIN32
#define TCHAR char
#define TEXT(str) (str)
#endif

#define DAEMON_NAME TEXT("ProActiveP2P")
#define KILL_CMD "killdaemon\n"
#define DAEMON_PORT 9015
#define DAEMON_LOG TEXT("[P2P] ")
#define DAEMON_USER TEXT("proacp2p")

void p2p_daemon(int argc, TCHAR * argv[]);

#ifdef WIN32

#define LOG_INFO 0
#define LOG_NOTICE 1
#define DEFAULT_PRG TEXT("..\..\scripts\windows\p2p\daemon.bat")
#define DAEMON_DIR TEXT("daemon.txt")

void Error(void);

#else

#define DEFAULT_PRG "../../scripts/unix/p2p/daemon.sh"
#define DAEMON_DIR  "daemon"

#endif
#endif
