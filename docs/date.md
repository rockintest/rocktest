# Date module

### *now* : to get the current date/time

---

#### Parameters

|   Name        | Usage                                    | Type      | Optional | Default value           |
| ------------- | ---------------------------------------- | ----------|----------|-------------------------|
| format        | Date / Time format                       | string    | Yes      | dd/MM/yyyy              |
| timeZone      | TimeZone                                 | string    | Yes      | System default timezone |

- format : format used by SimpleDateFormat. See https://docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html
- timeZone : example : UTC, UTC+1 or Europe/Paris

#### Output variable

    ${now.result}

#### Actions

Gets the current date/time and formats it.

#### Template

##### Expanded syntax

With default parameters

```yaml
- date.now:
- display: ${now.result}
```

With all the parameters

```yaml
- date.now:
  params:
    format:"HH:mm:ss"
    timeZone:"UTC"
  
- display: ${now.result}
```

#### Example (compact)

With default parameters:

```yaml
- display: ${$date.now()}
```

With format only:

```yaml
- display: ${$date.now(HH:mm:ss)}
```

With format and timeZone:

```yaml
- display: ${$date.now(HH:mm:ss,UTC)}
```

With timeZone only (parameter has to be named explicitly):

```yaml
- display: ${$date.now(timezone->UTC)}
```

With all parameters:

```yaml
- display: ${$date.now(timeZone->UTC+2,format->HH:mm:ss)}
```


### *minus* : subtract time

---

#### Parameters


|   Name        | Usage                                    | Type      | Optional | Default value           |
| ------------- | ---------------------------------------- | ----------|----------|-------------------------|
| date          | Date/Time to start from                  | string    | Yes      | Current date/time       |
| years         | Number of years to subtract              | string    | Yes      | 0                       |
| months        | Number of years to subtract              | string    | Yes      | 0                       |
| days          | Number of days to subtract               | string    | Yes      | 0                       |
| hours         | Number of hours to subtract              | string    | Yes      | 0                       |
| minutes       | Number of minutes to subtract            | string    | Yes      | 0                       |
| seconds       | Number of seconds to subtract            | string    | Yes      | 0                       |
| format        | Date / Time format                       | string    | Yes      | dd/MM/yyyy              |

- format : format used by SimpleDateFormat. See https://docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html
- date : date, in the format corresponding to the _format_ param.

#### Output variable

    ${minus.result}

#### Actions

Subtracts the amount of time, and puts the result in the variable

`Warning`: The _time_ part is optional. If the format does not contain a time, it is considered 00:00:00.
However, the _date_ part is mandatory. If you want to subtract from just a time (not a date), use the _time_
module (time.minus step).

#### Template

##### Expanded syntax


```yaml
- date.minus:
  params:
  date: 25/12/2020 12:01:01
  format: dd/MM/yyyy HH:mm:ss
  hours: 1
  minutes: 1
  seconds: 1
  years: 1
  months: 1
  days: 1
```


#### Example (compact)

Example to subtract 1 day from date 2020-12-25

```yaml
- display: ${$date.minus(date->2020-12-25,format->yyyy-MM-dd,days->1)}
```


### *plus* : adds time

---

#### Parameters


|   Name        | Usage                                    | Type      | Optional | Default value           |
| ------------- | ---------------------------------------- | ----------|----------|-------------------------|
| date          | Date/Time to start from                  | string    | Yes      | Current date/time       |
| years         | Number of years to add                   | string    | Yes      | 0                       |
| months        | Number of years to add                   | string    | Yes      | 0                       |
| days          | Number of days to add                    | string    | Yes      | 0                       |
| hours         | Number of hours to add                   | string    | Yes      | 0                       |
| minutes       | Number of minutes to add                 | string    | Yes      | 0                       |
| seconds       | Number of seconds to add                 | string    | Yes      | 0                       |
| format        | Date / Time format                       | string    | Yes      | dd/MM/yyyy              |

- format : format used by SimpleDateFormat. See https://docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html
- date : date, in the format corresponding to the _format_ param.

#### Output variable

    ${plus.result}

#### Actions

Adds the amount of time, and puts the result in the variable

`Warning`: The _time_ part is optional. If the format does not contain a time, it is considered 00:00:00.
However, the _date_ part is mandatory. If you want to subtract from just a time (not a date), use the _time_
module (time.minus step).

#### Template

##### Expanded syntax

```yaml
- date.plus:
  params:
  date: 25/12/2020 12:01:01
  format: dd/MM/yyyy HH:mm:ss
  hours: 1
  minutes: 1
  seconds: 1
  years: 1
  months: 1
  days: 1
```


#### Example (compact)

Example to add 1 day from date 2020-12-25

```yaml
- display: ${$date.plus(date->2020-12-25,format->yyyy-MM-dd,days->1)}
```
