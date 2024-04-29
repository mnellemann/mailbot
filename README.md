# Mail Bot

Mailbot is an SMTP to Telegram Gateway.

- Starts an embedded SMTP server, waits for (any email) and forwards to a specific Telegram chat
- Useful if you have systems or services that insists on using SMTP for notifications, alerts, etc.
- Use only on protected/closed networks with no public access (to mailbot on port 25)
- Parses plain/text emails with an optional image attached.

## Installation

- Requires Java runtime version 8 or later.
- [Download](https://github.com/mnellemann/mailbot/releases) and install the .rpm or .deb package and execute ```/opt/mailbox/bin/mailbot```
- Or [download](https://github.com/mnellemann/mailbot/releases) the .jar file and execute ```java -jar /path/to/mailbot-x.y.z-all.jar```

## Usage

```shell
Usage: mailbot [-hV] -i=<chatId> [-p=<port>] -t=<token>
  -h, --help               Show this help message and exit.
  -i, --chat-id=<chatId>   Telegram Chat ID.
  -p, --port=<port>        SMTP Port [default: 25].
  -t, --token=<token>      Telegram Token.
  -V, --version            Print version information and exit.
```
