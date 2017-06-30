
# MCVE for Selenium server stuck at creating a session

# Problem description

The Selenium server freezes/hangs when trying to start a new session through `POST /wd/hub/session`. The last message the server shows is

# Observations

```
19:50:49.326 INFO - Executing: [new session: Capabilities [{browserName=chrome}]])
19:50:49.327 INFO - Creating a new session for Capabilities [{browserName=chrome}]
Starting ChromeDriver 2.29.461571 (8a88bbe0775e2a23afda0ceaf2ef7ee74e822cc5) on port 28787
Only local connections are allowed.
```

The thread dump shows a thread stuck `RUNNABLE` at `socketRead0`:

```
"Forwarding newSession on session null to remote" #102 prio=5 os_prio=0 tid=0x00007f461c0bc800 nid=0xca8 runnable [0x00007f46673f0000]
   java.lang.Thread.State: RUNNABLE
        at java.net.SocketInputStream.socketRead0(Native Method)
        at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
        at java.net.SocketInputStream.read(SocketInputStream.java:170)
        at java.net.SocketInputStream.read(SocketInputStream.java:141)
        at org.apache.http.impl.io.SessionInputBufferImpl.streamRead(SessionInputBufferImpl.java:137)
        at org.apache.http.impl.io.SessionInputBufferImpl.fillBuffer(SessionInputBufferImpl.java:153)
        at org.apache.http.impl.io.SessionInputBufferImpl.readLine(SessionInputBufferImpl.java:282)
        at org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead(DefaultHttpResponseParser.java:138)
        at org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead(DefaultHttpResponseParser.java:56)
        at org.apache.http.impl.io.AbstractMessageParser.parse(AbstractMessageParser.java:259)
        at org.apache.http.impl.DefaultBHttpClientConnection.receiveResponseHeader(DefaultBHttpClientConnection.java:163)
        at org.apache.http.impl.conn.CPoolProxy.receiveResponseHeader(CPoolProxy.java:165)
        at org.apache.http.protocol.HttpRequestExecutor.doReceiveResponse(HttpRequestExecutor.java:273)
        at org.apache.http.protocol.HttpRequestExecutor.execute(HttpRequestExecutor.java:125)
        at org.apache.http.impl.execchain.MainClientExec.execute(MainClientExec.java:272)
        at org.apache.http.impl.execchain.ProtocolExec.execute(ProtocolExec.java:185)
        at org.apache.http.impl.execchain.RetryExec.execute(RetryExec.java:89)
        at org.apache.http.impl.execchain.RedirectExec.execute(RedirectExec.java:111)
        at org.apache.http.impl.client.InternalHttpClient.doExecute(InternalHttpClient.java:185)
        at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:72)
        at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:56)
        at org.openqa.selenium.remote.internal.ApacheHttpClient.fallBackExecute(ApacheHttpClient.java:139)
        at org.openqa.selenium.remote.internal.ApacheHttpClient.execute(ApacheHttpClient.java:87)
        at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:343)
        at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:159)
        at org.openqa.selenium.remote.HttpCommandExecutor.execute(HttpCommandExecutor.java:142)
        at org.openqa.selenium.remote.service.DriverCommandExecutor.execute(DriverCommandExecutor.java:82)
        at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:637)
        at org.openqa.selenium.remote.RemoteWebDriver.startSession(RemoteWebDriver.java:250)
        at org.openqa.selenium.remote.RemoteWebDriver.startSession(RemoteWebDriver.java:236)
        at org.openqa.selenium.remote.RemoteWebDriver.<init>(RemoteWebDriver.java:137)
        at org.openqa.selenium.chrome.ChromeDriver.<init>(ChromeDriver.java:184)
        at org.openqa.selenium.chrome.ChromeDriver.<init>(ChromeDriver.java:148)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
        at org.openqa.selenium.remote.server.DefaultDriverProvider.callConstructor(DefaultDriverProvider.java:102)
        at org.openqa.selenium.remote.server.DefaultDriverProvider.newInstance(DefaultDriverProvider.java:96)
        at org.openqa.selenium.remote.server.DefaultDriverFactory.newInstance(DefaultDriverFactory.java:60)
        at org.openqa.selenium.remote.server.DefaultSession$BrowserCreator.call(DefaultSession.java:222)
        at org.openqa.selenium.remote.server.DefaultSession$BrowserCreator.call(DefaultSession.java:209)
        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
        at org.openqa.selenium.remote.server.DefaultSession$1.run(DefaultSession.java:176)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at java.lang.Thread.run(Thread.java:745)
```

The `POST /wd/hub/session` request is completely written to the TCP socket and the segment is properly `ACK`ed (acknowledged):

```
POST /wd/hub/session HTTP/1.1
Content-Type: application/json; charset=utf-8
Content-Length: 312
Host: localhost:4444
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.5.1 (Java/1.8.0_92)
Accept-Encoding: gzip,deflate

{
  "desiredCapabilities": {
    "browserName": "chrome"
  },
  "requiredCapabilities": {},
  "capabilities": {
    "desiredCapabilities": {
      "browserName": "chrome"
    },
    "requiredCapabilities": {},
    "alwaysMatch": {},
    "firstMatch": [
      {
        "browserName": "chrome"
      }
    ]
  }
}
```


# Setup

## Hard- and software

- `Linux hostname 4.4.0-71-generic #92-Ubuntu SMP Fri Mar 24 12:59:01 UTC 2017 x86_64 x86_64 x86_64 GNU/Linux`
- OpenJDK 1.8.0_92
- Selenium client v3.3.1 and v3.4.0 (only these tested)
- Selenium server v3.4.0
- ChromeDriver v2.28 and v2.29
- Chrome 58
- Chrome 59 is unaffected
- Firefox 54 and Geckodriver 0.16.1 seem to be unaffected (no issues in 100 iterations)

## Server started with

```
$ /opt/jdk8//bin/java \
	-Dwebdriver.chrome.driver=/home/dev/opt/selenium/drivers/chrome/chromedriver-2.29/chromedriver \
	-Dwebdriver.gecko.driver=/home/dev/opt/selenium/drivers/gecko/geckodriver-0.16.1/geckodriver \
	-jar /home/dev/opt/selenium/selenium/selenium-server-standalone-3.4.0.jar \
```

## Known so far:

- concurrency is not an issue - happens single threaded too
- no mouse movement or keyboard interaction is required
- it requires different amount of iterations until the issue happens
- it usually happens within 5..50 iterations

