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
