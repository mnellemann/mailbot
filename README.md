# Mail Bot

_This is only for protected/closed networks with no public access (to mailbot on port 25)._

**What**

Start an embedded SMTP server, wait for (any email) and forward it to a specific Telegram chat.

**Why**

Useful for those older systems that insists on using SMTP for notifications, alerts, etc.


## Usage

```shell
Usage: mailbot [-hV] -i=<chatId> [-p=<port>] -t=<token>
  -h, --help               Show this help message and exit.
  -i, --chat-id=<chatId>   Telegram Chat ID.
  -p, --port=<port>        SMTP Port [default: 25].
  -t, --token=<token>      Telegram Token.
  -V, --version            Print version information and exit.
```

## Installation

Requires Java runtime version 8 or later.

Download and install .rpm or .deb package, install and execute ```/opt/mailbox/bin/mailbot```.

Or download .jar file and execute ```java -jar /path/to/mailbot-x.y.z.jar```.