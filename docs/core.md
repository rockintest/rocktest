# RockTest core features

* TOC {:toc}

## Scenario structure

Scenario are divided into steps. The template of a step is  the following:

```yaml
- step: <TYPE OF STEP>
  desc: <Description of the step> (this is optionnal)
  name: <Name used by she setp, if needed>
  value: <value of the step, if needed>
  params:
    Map of the step parameters
```

According to the step types, some of those attributes are mandatory, and some others are unused. 

`Note`: you can choose the compact notation. In this case, the _value_ parameter is passed together with
the _step_ parameter :

```yaml
- step: <TYPE OF STEP> <value of the step>
  desc: <Description of the step> (this is optionnal)
  name: <Name used by she setp, if needed>
  params:
    Map of the step parameters
```

## Core step types

Some step types are builtin, some others are part of modules. Here are the core functions.

### *title* : to set a title to your scenario

---

#### Parameters

|   Name        | Usage                                    | Type      | Optional |
| ------------- | ---------------------------------------- | ----------|----------|
| desc          | Step description, for logs and report    | string    | Yes      |
| value         | The title of the scenario                | string    | No       |

#### Actions

Set the title, which is used in the logs and in test reports. It is a good practice to set a title to your scenario,
but it is optional.

#### Example

```yaml
- step: title
  value: Title of my Rock Scenario
```

#### Example (compact)

```yaml
- step: title Title of my Rock Scenario
```


### *display* : to set a title to your scenario

---

#### Parameters

|   Name        | Usage                                    | Type      | Optional |
| ------------- | ---------------------------------------- | ----------|----------|
| desc          | Step description, for logs and report    | string    | Yes      |
| value         | Value to display                         | string    | No       |

#### Actions

Displays a message in the output pf the scenario.

#### Example

_Scenario_

```yaml
- step: dislplay
  value: Rock message to display
```

#### Example (compact)

_Scenario_

```yaml
- step: dislplay Rock message to display
```


_Output_

```
___  __ \______ __________  /_____  __/_____ __________  /_
__  /_/ /_  __ \_  ___/__  //_/__  /   _  _ \__  ___/_  __/
_  _, _/ / /_/ // /__  _  ,<   _  /    /  __/_(__  ) / /_
/_/ |_|  \____/ \___/  /_/|_|  /_/     \___/ /____/  \__/
 Test automation that rocks !        (v1.0.0-SNAPSHOT)
09/01/2021 21:14:48.803 [INFO ] -  Starting RocktestApplication v1.0.0-SNAPSHOT on bouzin with PID 76450 (/home/ben/src/rock/rocktest/target/rocktest-1.0.0-SNAPSHOT.jar started by ben in /home/ben/src/rock/rocktest/docs/example)
09/01/2021 21:14:48.805 [DEBUG] -  Running with Spring Boot v2.3.0.RELEASE, Spring v5.2.6.RELEASE
09/01/2021 21:14:48.805 [INFO ] -  No active profile set, falling back to default profiles: default
09/01/2021 21:14:49.397 [INFO ] -  Started RocktestApplication in 0.882 seconds (JVM running for 1.266)
09/01/2021 21:14:49.398 [INFO ] -  Set variable module = display
09/01/2021 21:14:49.399 [INFO ] -  Load scenario. name=display.yaml, dir=.
09/01/2021 21:14:49.422 [INFO ] -  Set variable module = display
----------------------------------------
09/01/2021 21:14:49.424 [INFO ] - [display] Step#1 (Display test) display,Hello RockTest
09/01/2021 21:14:49.428 [INFO ] - [display] Step#1 Hello RockTest
----------------------------------------
========================================
=     Scenario Success ! It Rocks      =
========================================
```

### *pause* : wait for a delay

---

#### Parameters

|   Name        | Usage                                    | Type      | Optional |
| ------------- | ---------------------------------------- | ----------|----------|
| desc          | Step description, for logs and report    | string    | Yes      |
| value         | Delay to wait in seconds                 | int       | No       |

#### Actions

Suspends the scenario.

#### Example

_Template_

```yaml
- step: pause
  value: 10
```

OR

```yaml
- step: pause 10
```


_Full example_

- [pause.yaml](example/pause.yaml)

## Debug your scenarios

### Log level

You can change the log level by setting the ROCK_LOG_LEVEL variable. The default level is INFO.

- INFO : short step description, displays, errors, warning
- DEBUG : more detailed messages (values tested...)
- TRACE: each step is fully displayed

### Display messages

- Use display steps to print variables. 

### Skip steps

- Use an _exit_ step to stop a scenario in the middle

_Example_

```yaml
- step: display
  value: You can see me

- step: exit

- step: display
  value: You cannot see me
```

- Use _skip_ and _resume_ steps to skip a part of the scenario

_Example_

```yaml
- step: display
  value: You can see me

- step: skip

- step: display
  value: You cannot see me

- step: resume

- step: display
  value: You can see me again
```

- Skip a single step, by adding "--" in front of its type

```yaml
- step: display
  value: You can see me

- step: --display
  value: You cannot see me
```

## Modules

### _exec_ : Call a module

---

#### Parameters

|   Name        | Usage                                    | Type      | Optional              |
| ------------- | ---------------------------------------- | ----------|----------             |
| desc          | Step description, for logs and report    | string    | Yes                   |
| value         | module to call                           | string    | No                    |
| params        | parameters passed to the module          | map       | Depends on the module |         

#### Actions

Calls a Module with parameters

#### Example

_Template_

```yaml
- step: exec
  value: module.function
  params:
    <Map of the parameters>
```

_Template compact_

```yaml
- step: exec module.function
  params:
    <Map of the parameters>
```


The map params is specific to each module and is described in the module documentation. 

#### Module return value

The modules return data by putting variables in the caller context. The name of the variable depends on the module, 
and are described in the module documentation.

_Example_ : the _date.now_ function returns the current date in the "now.result" 

```yaml
- step: exec
  name: date.now

- step: display
  value: ${now.result}
```

## Call a sub-scenario

It is possible to create a sub-scenario (in another YAML file) and call it from a main scenario.

### _call_ : Execute another scenario

---

#### Parameters

|   Name        | Usage                                    | Type            | Optional |
| ------------- | ---------------------------------------- | ----------      |----------|
| desc          | Step description, for logs and report    | string          | Yes      |
| value         | Name of the scenario                     | string          | No       |
| params        | Map of string                            | string          | Yes      |

#### Actions

Calls the scenario. The called-scenario is searched in the same location as the main scenario.

The variables of the map _params_ are put as variables in the context of the sub-scenario. The sub-scenario 
does not "know" the other variables of the main script.

#### Example

##### Basic example

First, define a module _display.yaml_

```yaml
- step: display
  value: Hello from Rock Module
```

Then create a main scenario called _call.yaml_

```yaml
- step: call
  value: display
```

Run the main scenario :

    $ rocktest call.yaml

You will get the following result :

```
----------------------------------------
10/01/2021 17:26:28.986 [INFO ] - [call] Step#1 call,variable
10/01/2021 17:26:28.990 [INFO ] - [call] Step#1 Load scenario. name=./variable.yaml, dir=.
10/01/2021 17:26:28.991 [INFO ] - [call] Step#1 Set variable module = variable
----------------------------------------
10/01/2021 17:26:28.991 [INFO ] - [call/variable] Step#1 title,Variable example
----------------------------------------
10/01/2021 17:26:28.991 [INFO ] - [call/variable] Step#2 (Set the rock variable (this description is optional)) var,roll
10/01/2021 17:26:28.991 [INFO ] - [call/variable] Step#2 Set variable rock = roll
----------------------------------------
10/01/2021 17:26:28.992 [INFO ] - [call/variable] Step#3 display,Rock'n'${rock} => Rock'n'roll
10/01/2021 17:26:28.992 [INFO ] - [call/variable] Step#3 Rock'n'roll
----------------------------------------
```

The call stack is present in each line of log (here [call] for the main scenario and
[call/variable] for the sub scenario).


##### Example with parameters

Define a module _display2.yaml_

```yaml
- step: display
  value: Hello from Rock Module - ${message}
```

Then create a main scenario called _call2.yaml_

```yaml
- step: call
  value: display2
  params:
    message: it rocks
```

According to the YAML syntax, params is a Map. It contains 1 element "message" with value "it rocks".

If you run this scenario, you get the message :

  Hello from Rock Module - it rocks

#### Scenarios location

The scenarios can be located in a subdirectory. You can specify the path in the scenario name.
The root is the path of the main scenario.

```yaml
- step: call
  value: scenario/display
```

Executes the scenario located in scenario/display.yaml.

#### Sub-sub scenarios

You can another sub-scenario from a sub-scenario.
The search path is always the path of the first scenario called.

**Example** :

_main.yaml_

```yaml
- step: call scenarios/lib
```

_scenarios/lib.yaml_

```yaml
- step: call scenarios/sublib
```

_scenarios/sublib.yaml_

```yaml
- step: display Hello from sublib
```

When you execute the scenario, you get the following result :

```
----------------------------------------
10/01/2021 17:52:00.132 [INFO ] - [main] Step#1 call,scenarios/lib
10/01/2021 17:52:00.136 [INFO ] - [main] Step#1 Load scenario. name=./scenarios/lib.yaml, dir=.
10/01/2021 17:52:00.137 [INFO ] - [main] Step#1 Set variable module = lib
----------------------------------------
10/01/2021 17:52:00.137 [INFO ] - [main/lib] Step#1 call,scenarios/sublib
10/01/2021 17:52:00.137 [INFO ] - [main/lib] Step#1 Load scenario. name=./scenarios/sublib.yaml, dir=.
10/01/2021 17:52:00.138 [INFO ] - [main/lib] Step#1 Set variable module = sublib
----------------------------------------
10/01/2021 17:52:00.138 [INFO ] - [main/lib/sublib] Step#1 display,Hello from sublib
10/01/2021 17:52:00.138 [INFO ] - [main/lib/sublib] Step#1 Hello from sublib
----------------------------------------
```

Note the stack for the sublib : [main/lib/sublib].

When the _sublib_ scenario is called, the path specified is "scenarios", because the root
is always the path of the main scenarios, even when you call a scenario from another sub-scenario.

### _return_ : Returns a variable to the caller

---

#### Parameters

|   Name        | Usage                                    | Type            | Optional |
| ------------- | ---------------------------------------- | ----------      |----------|
| desc          | Step description, for logs and report    | string          | Yes      |
| name          | Name of the variable                     | string          | No       |
| value         | Value of the variable                    | string          | No       |

#### Actions

In a sub-scenario, _return_ allows to put a variable in the caller context.

If the _name_ parameters starts with a period, it will be put unchanged in the caller context (without
the period). 
 
    ${<name parameter>}

`warning`: if a variable ${<name parameter>} already exists in the caller context, it will be erased. Use
this feature with caution. Usually, is it safer to use the following option :

If the name of the variable does not start with a period, the result will be accessible as a variable called
    
    ${name of the scenario>.<name of the variable>}

It is possible to have multiple _return_ steps. The return step does not exit the sub-scenario.
A _return_ step in the main scenario (without caller) has no effect.

#### Example

_scenarios/libreturn.yaml_

````yaml
# First value, returned as ${libreturn.fromLib}
- step: return
  name: fromLib
  value: It comes from the library

# Second value returned as {fromLib2}
- step: return
  name: .fromLib2
  value: It comes again from the library
````

_mainreturn.yaml_

````yaml
- step: call
  value: scenarios/libreturn

# Displays "It comes from the library"
- step: display
  value: ${libreturn.fromLib}

# Displays "It comes again from the library"
- step: display
  value: ${fromLib2}

# Displays "${fromLib}" because the variable is not defined
- step: display
  value: ${fromLib}
````

#### Example : compact notation

_scenarios/libreturncompact.yaml_

````yaml
# First value, returned as ${libreturn.fromLib}
- step: return  fromLib = It comes from the library

# Second value returned as {fromLib2}
- step: return  .fromLib2 = It comes again from the library
````

_mainreturncompact.yaml_

````yaml
- step: call scenarios/libreturn.yaml

# Displays "It comes from the library"
- step: display ${libreturn.fromLib}

# Displays "It comes again from the library"
- step: display ${fromLib2}

# Displays "${fromLib}" because the variable is not defined
- step: display ${fromLib}
````


## Functions

It is possible to define a function in your scenario, to avoid duplication of some steps or
make your scenario more readable.

A function is like a sub-scenario, but located in the same YAML file.

### _function_ : Defines a function

---

#### Actions

Defines a function in the local context

#### Parameters

|   Name        | Usage                                    | Type            | Optional |
| ------------- | ---------------------------------------- | ----------      |----------|
| desc          | Step description, for logs and report    | string          | Yes      |
| name          | the name of the function                 | string          | No       |
| steps         | the steps of the function                | list of steps   | No       |

#### Example

````yaml
- step: function
  name : func1
  steps:
    - step: display
      value : func1 called with param ${message}

    - step: return ret = func1 returns Hello
````

### _call_ : To call a function

Functions are called like scenarios. To specify you want to call a function instead og a scenario, use "->" as a prefix of
the call argument.

#### Example

````yaml
# Calls the scenario defined above, in the same YAML scenario
- step: call
  value: ->func1
  params:
    message: This is the parameter

# Gets the result
- step: display ${func1.ret}
````

It is also possible to call a function located in another scenario :

#### Example

_scenarios/functions.yaml_

This scnario contains 2 functions :

````yaml
- step: function
  name : func1
  steps:
    - step: display
      value : func1 called with param ${message}

    - step: return ret = func1 returns Hello

- step: function
  name : func2
  steps:
    - step: display
      value : func2 called with param ${message}

    - step: return ret = func2 returns Hello
````

_function-external.yaml_

````yaml
# Calls the function func1 defined in another scenario
# Should display "func1 called with param This is the parameter"
- step: call scenarios/functions->func1
  params:
    message: This is the parameter

# Gets the result. In this case, the result is in a variable
# <scenario>.<function>.<variable>
- step: display ${functions.func1.ret}
````


## Variables and expressions

### _var_ : Define a variable

---

#### Actions

Defines a variable in the local context

#### Parameters

|   Name        | Usage                                    | Type            | Optional |
| ------------- | ---------------------------------------- | ----------      |----------|
| desc          | Step description, for logs and report    | string          | Yes      |
| name          | the name of the variable                 | string          | No       |
| value         | the value of the variable                | string or int   | No       |

#### Example

_Template_

```yaml
- step: var
  name: rock
  value: This is Rock'n'Roll
```

_Template : compact notation_

```yaml
- step: var rock = This is Rock'n'Roll
```


_Full example_

- [variable.yaml](example/variable.yaml)

### Expressions

---

_expressions_ are useful, and allow advanced string substitution.

An expression is of the form :

    ${<expression>}

#### Expand a variable

The expression

    ${var}

will be resolved as the content of the variable _var_ if is exists. If it does not exist, it is not replaced, and the expression remains unchanged.

_Example_

```yaml
- step: var
  name: rock
  value: roll

# Displays "roll"
- step: display
  value: ${rock}

# Displays "${jazz}"
- step: display
  value: ${jazz}
```

#### Specify a default value

If the variable is not set, a default value can be used. Use the syntax :

    ${<varname>::<default value>}

_Example_

```yaml
# Displays "unset"
- step: display
  value: ${jazz::unset}
```

The default value can itself be an expression.

```yaml
- step: var
  name: default
  value: music

# Displays "music"
- step: display
  value: ${jazz::${default}}
```


#### Specify a value if set and a value if not set

An expression can be replaced by a value when a variable is set, and by another expression if the variable os not set.

    ${<varname>?<expression if set>::<expression if not set>}

_Example_

```yaml
# Displays "unset value"
- step: display
  value: ${jazz?Set value=${jazz}::unset value}
```

```yaml
- step: var
  name: rock
  value: roll

# Displays "Set value=roll"
- step: display
  value: ${rock?Set value=${rock}::unset value}
```

#### Call a module inline

With expressions, you can call a module in one line, and the result is processed as a variable.

For example, the _date.now_ function, part of the module _date_ can be called as follows :

```yaml
# Displays the current date
- step: display
  value: ${$date.now()}
```

The template are :

```
${$module.function(param1,param2...)}
```

The parameters depend on the module. See the documentation of each module.

If the module has optional parameters, you can name them and pass only a part of them.

For example, for the module "rock.music" with 2 parameters : instrument and band. 
You can call the module with the syntax above :

```
${$rock.music(guitar,dire straits)}
```

If you want to pass only 1 parameter, you can name it :

```
${$rock.music(band:=dire straits)}
```
or
```
${$rock.music(band:=dire straits,instrument:=guitar)}
```




