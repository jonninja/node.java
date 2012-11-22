Node.Java is a Java framework inspired by many of the ideas in Node.JS and the Express framework.

Part of the appeal of Node.js is the ability to create web applications that are easy to understand, and
do so extremely quickly. For example, check out a node.js 'Hello World' application:

    var express = require('express'),
    app = express.createServer();
    app.get('/', function(req, res){
      res.send('Hello World');
    });
    app.listen(3000);

What makes this appealing isn't the few lines of code - a servlet based Hello World might not be that
much more code. But the Express version is extremely easy to follow. There are not configuration files.
There are no annotations. On larger scale applications, this might not matter much, but there is an
enormous appeal to this kind of programming model, if the popularity of Node is any indication.

Here's a Node.Java version of the same application:

    public class HelloWorld {
      public static void main(String[] args) {
        Express express = new Express();
        express.get("/", new Express.Handler() {
          public void exec(Request req, Response res, Express.Next next) {
            res.send("Hello World");
          }
        });
      }
    }

Java requires that there be a little bit more code, but it maintains the clarity and simplicity
of the Node.js version. Eventually, Lambda support in JDK8 will make the code even simpler.