# RockTest core features

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

## Core step types

Some step types are builtin, some others are part of modules. Here are the core functions.

### *title* : to set a title to your scenario

#### Actions

Set the title, which is used in the logs and in test reports. It is a good practice to set a title to your scenario,
but it is optional.

#### Parameters

|   Name        | Usage                                    | Type      | Optional |
| ------------- | ---------------------------------------- | ----------|----------|
| value         | The title of the scenario                | string    | No       |
| step          | Step description, for logs and report    | string    | Yes      |

#### Example

```yaml
- step: title
  value: Title of my Rock Scenario
```

### *display* : to set a title to your scenario

#### Actions

Displays a message in the output pf the scenario.

#### Parameters

|   Name        | Usage                                    | Type      | Optional |
| ------------- | ---------------------------------------- | ----------|----------|
| value         | Value to display                         | string    | No       |
| step          | Step description, for logs and report    | string    | Yes      |

#### Example

_Scenario_

```yaml
- step: dislplay
  value: Rock message to display
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

#### Actions

Suspends the scenario.

#### Parameters

|   Name        | Usage                                    | Type      | Optional |
| ------------- | ---------------------------------------- | ----------|----------|
| value         | Delay to wait in seconds                 | int       | No       |
| step          | Step description, for logs and report    | string    | Yes      |

#### Example

_Template_

```yaml
- step: pause
  value: 10
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

## Variables and expressions

### _var_ : Define a variable

#### Actions

Defines a variable in the local context

#### Parameters

|   Name        | Usage                                    | Type            | Optional |
| ------------- | ---------------------------------------- | ----------      |----------|
| name          | the name of the variable                 | string          | No       |
| value         | the value of the variable                | string or int   | No       |
| step          | Step description, for logs and report    | string          | Yes      |

#### Example

_Template_

```yaml
- step: var
  name: rock
  value: roll
```
_Full example_

- [variable.yaml](example/variable.yaml)

### Expressions

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

With expressions, you can call a module in one line, and the result is processed as a variable


