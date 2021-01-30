# Time module

### *now* : to get the current time

---

#### Parameters

|   Name        | Usage                                    | Type      | Optional | Default value           |
| ------------- | ---------------------------------------- | ----------|----------|-------------------------|
| format        | Time format                              | string    | Yes      | HH:mm:ss                |
| timeZone      | TimeZone                                 | string    | Yes      | System default timezone |

- format : format used by SimpleDateFormat. See https://docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html
- timeZone : example : UTC, UTC+1 or Europe/Paris

#### Output variable

    ${now.result}

#### Actions

Gets the current time and formats it.

#### Template

##### Expanded syntax

With default parameters

```yaml
- time.now:
- display: ${now.result}
```

With all the parameters

```yaml
- time.now:
  params:
    format:"HH:mm:ss"
    timeZone:"UTC"
  
- display: ${now.result}
```

#### Example (compact)

With default parameters:

```yaml
- display: ${$time.now()}
```

With format only:

```yaml
- display: ${$time.now(HH:mm:ss)}
```

With format and timeZone:

```yaml
- display: ${$time.now(HH:mm:ss,UTC)}
```

With timeZone only (parameter has to be named explicitly):

```yaml
- display: ${$time.now(timezone->UTC)}
```

With all parameters:

```yaml
- display: ${$time.now(timeZone->UTC+2,format->HH:mm:ss)}
```


### *minus* : subtract time

---

#### Parameters


|   Name        | Usage                                    | Type      | Optional | Default value           |
| ------------- | ---------------------------------------- | ----------|----------|-------------------------|
| time          | Time to start from                       | string    | Yes      | Current time            |
| hours         | Number of hours to subtract              | string    | Yes      | 0                       |
| minutes       | Number of minutes to subtract            | string    | Yes      | 0                       |
| seconds       | Number of seconds to subtract            | string    | Yes      | 0                       |
| format        | Time format                              | string    | Yes      | dd/MM/yyyy              |

- format : format used by SimpleDateFormat. See https://docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html
- time : time, in the format corresponding to the _format_ param.

#### Output variable

    ${minus.result}

#### Actions

Subtracts the amount of time, and puts the result in the variable

If you need to manipulate dates, use the date module.

#### Template

##### Expanded syntax


```yaml
- time.minus:
  params:
  time: 12:01:01
  hours: 1
  minutes: 1
  seconds: 1
```


#### Example (compact)

Example to subtract 1 hour from time 12:00:00

```yaml
- display: ${$time.minus(time->12:00:00,hours->1)}
```


### *plus* : add time

---

#### Parameters


|   Name        | Usage                                    | Type      | Optional | Default value           |
| ------------- | ---------------------------------------- | ----------|----------|-------------------------|
| date          | Time to start from                       | string    | Yes      | Current date/time       |
| hours         | Number of hours to add                   | string    | Yes      | 0                       |
| minutes       | Number of minutes to add                 | string    | Yes      | 0                       |
| seconds       | Number of seconds to add                 | string    | Yes      | 0                       |
| format        | Date / Time format                       | string    | Yes      | HH:mm:ss                |

- format : format used by SimpleDateFormat. See https://docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html
- time : time, in the format corresponding to the _format_ param.

#### Output variable

    ${plus.result}

#### Actions

Adds the amount of time, and puts the result in the variable

Use date module if you want to manipulate dates.

#### Template

##### Expanded syntax

```yaml
- time.plus:
  params:
  time: 12:01:01
  hours: 1
  minutes: 1
  seconds: 1
```


#### Example (compact)

Example to add 1 hour to time 12:00:00

```yaml
- display: ${$time.plus(time->12:00:00,hours->1)}
```
