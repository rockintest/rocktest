# RockTest

![rocktest.png](rocktest.png)

<iframe width="560" height="315"
src="https://www.youtube.com/embed/YQBpYTihZQM" 
frameborder="0" 
allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" 
allowfullscreen></iframe>

## What is it ?

RockTest allows you to write test scenarios, using a simple and well known YAML syntax.

Tests are divided into steps, which are YAML blocks.

Example :

````yaml
- display: This is RockTest
- sql.request:
  params:
    request: select * from rock
````

This scenario has 2 steps :
- A display to print a message
- A sql.request to execute a SQL request

Many modules are available (HTTP clients and server, SQL, Web automation with Selenium, Assertions...)

## Interested ? Rock with _Katakoda_ !

See it live on Katakoda : https://www.katakoda.com/rocktest

## Getting started with Docker

You can use Rocker, the packaged Docker image of RockTest.

To run a scenario, do the following commands (suppose $SCEN is the directory where your senarios are)

    $ cd $SCEN
    $ docker run -it --rm -v $PWD:/scen rockintest/rocktest scenario.yaml

*Example*:

To execute a simple scenario :

    $ git clone https://github.com/rockintest/rocktest.git
    $ cd rocktest/docs/example/core
    $ docker run -it --rm -v $PWD:/scen rockintest/rocktest display.yaml

## Getting started without Docker

Get the binary release :

    $ curl -L https://github.com/rockintest/rocktest/releases/latest/download/rocktest-bin.tar.gz --output rocktest-bin.tar.gz

Extract the archive

    $ tar xzvf rocktest-bin.tar.gz

Setup the environment :

    $ rocktest-bin/sh/rocksetup
    $ . ~/.bashrc

Run the "display.yaml" scenario

    $ rocktest rocktest-bin/core/display.yaml

You Rock !! 

## Build Rocktest

### Prerequisites

To run RockTest, you need the following :

#### Setup Java 8

- Download and install the latest JDK 8 from [https://adoptopenjdk.net/](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot).
Make sure java and javac are in the PATH.

#### Setup Maven >= 3.6.3

- Download the last version from : https://maven.apache.org/download.cgi.
- Extract the ZIP, and add the bin sub-directory to your PATH.

#### Setup Selenium

As an option, to run the Web tests, you will need a Selenium server for your preferred browser.
- For Chrome : https://chromedriver.chromium.org/downloads
- For Firefox : https://github.com/mozilla/geckodriver/releases

Extract the binary and put it in your PATH.
You need the browser installed too, of course (chrome and/or Firefox).

#### A Shell

If you are running Linux then you are ready to Rock :guitar:

For Windows users, you can use either use WSL or Gitbash as a shell : https://gitforwindows.org/

This will install Git and Bash on your Windows box.

### Build from source

Open your shell, go to the right directory and run the following commands :

    $ git clone https://github.com/rockintest/rocktest.git
    $ cd rocktest
    $ mvn clean package

Then set the `$ROCK_HOME` environnement variable and add the `sh` directory to your PATH :

    $ export ROCK_HOME=$PWD
    $ export PATH=$PATH:$ROCK_HOME/sh

### Run your first scenario

Enter the following comands :

    $ cd $ROCK_HOME/scen-test
    $ rocktest hello.yaml

You should see some logs, with the message "Hello RockTest" and a scenario success

```
________               ______  ________              _____
___  __ \______ __________  /_____  __/_____ __________  /_
__  /_/ /_  __ \_  ___/__  //_/__  /   _  _ \__  ___/_  __/
_  _, _/ / /_/ // /__  _  ,<   _  /    /  __/_(__  ) / /_
/_/ |_|  \____/ \___/  /_/|_|  /_/     \___/ /____/  \__/
 Test automation that rocks !        (v1.0.0-SNAPSHOT)
09/01/2021 16:08:15.559 [INFO ] -  Starting RocktestApplication v1.0.0-SNAPSHOT on bouzin with PID 69786 (/home/ben/src/rock/rocktest/target/rocktest-1.0.0-SNAPSHOT.jar started by ben in /home/ben/src/rock/rocktest/scen-test)
09/01/2021 16:08:15.560 [DEBUG] -  Running with Spring Boot v2.3.0.RELEASE, Spring v5.2.6.RELEASE
09/01/2021 16:08:15.561 [INFO ] -  No active profile set, falling back to default profiles: default
09/01/2021 16:08:16.136 [INFO ] -  Started RocktestApplication in 0.893 seconds (JVM running for 1.288)
09/01/2021 16:08:16.138 [INFO ] -  Set variable module = hello
09/01/2021 16:08:16.138 [INFO ] -  Load scenario. name=hello.yaml, dir=.
09/01/2021 16:08:16.161 [INFO ] -  Set variable module = hello
----------------------------------------
09/01/2021 16:08:16.163 [INFO ] - [hello] Step#1 display,Hello RockTest !
09/01/2021 16:08:16.167 [INFO ] - [hello] Step#1 Hello RockTest !
----------------------------------------
========================================
=     Scenario Success ! It Rocks      =
========================================
```

### Congrats !

You have just run your first RockTest scenario.

To learn how to do HTTP requests, SQL requests, Web scenarios or HTTP mocks, check the documentation : https://rockintest.github.io/rocktest/

Or play live on Katakoda : https://katakoda.com/rocktest

Have fun with RockTest !
