#ifdef WIN32

#include <windows.h>
#include <tchar.h>
#include <io.h>

#else

#include <unistd.h>             /* close() */
#include <sys/socket.h>         /* socket(), bind(), connect(), send() */
#include <netinet/in.h>         /* struct sockaddr_in, htons() */
#include <netdb.h>              /* struct hostent, gethostbyname() */

#endif

#include <stdio.h>              /* stderr, fprintf() */
#include <string.h>             /* memcpy(), strlen() */

#include "../common/proactivep2p.h"
#define COMMAND_END "\n"

#ifdef WIN32

#define perror(str) PrintError(str)

static void PrintError(const char *str)
{
    LPVOID lpMsgBuf;
    DWORD dw = GetLastError();

    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
                  FORMAT_MESSAGE_FROM_SYSTEM,
                  NULL,
                  dw,
                  MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                  (LPTSTR) & lpMsgBuf, 0, NULL);

    printf("%s: %d: ", str, dw);
    _tprintf(TEXT("%s\n"), lpMsgBuf);

    LocalFree(lpMsgBuf);
}
#endif

int main(int argc, char **argv)
{
    int sd, rc;
    struct sockaddr_in localAddr, servAddr;
    struct hostent *h;
    const char *hostname;
    char ok[3];

    if (argc < 2) {
        fprintf(stderr, "Usage: %s (stop|restart|alive|killdaemon|flush) [HOST]\n",
                argv[0]);
        return 1;
    }

    if (argc == 3) {
        hostname = argv[2];
    } else {
        hostname = "localhost";
    }

#ifdef WIN32
    {
        WSADATA wsaData;
        if (WSAStartup(MAKEWORD(2, 0), &wsaData) != 0)
            return 1;
    }
#endif

    h = gethostbyname(hostname);
    if (h == NULL) {
        fprintf(stderr, "%s: unknown host '%s'\n", argv[0], hostname);
        return 2;
    }

    servAddr.sin_family = h->h_addrtype;
    memcpy((char *) &servAddr.sin_addr.s_addr, h->h_addr_list[0], h->h_length);
    servAddr.sin_port = htons(DAEMON_PORT);

    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) {
        perror("socket");
        return 3;
    }

    /* bind any port number */
    localAddr.sin_family = AF_INET;
    localAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    localAddr.sin_port = htons(0);

    rc = bind(sd, (struct sockaddr *) &localAddr, sizeof(localAddr));
    if (rc < 0) {
        perror("bind");
        return 4;
    }

    /* connect to server */
    rc = connect(sd, (struct sockaddr *) &servAddr, sizeof(servAddr));
    if (rc < 0) {
        perror(hostname);
        return 5;
    }

    if (send(sd, argv[1], strlen(argv[1]), 0) < 0 ||
        send(sd, COMMAND_END, strlen(COMMAND_END), 0) < 0) {

        perror(hostname);
        return 6;
    }

    ok[2] = '\0';

    if (recv(sd, ok, 2, 0) != 2 || strcmp("OK", ok)) {
        fprintf(stderr, "unknown command: %s\n", argv[1]);
        return 7;
    }

    close(sd);
    printf("%s(%s): OK\n", argv[1], hostname);
    return 0;
}
