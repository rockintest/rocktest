# Create HTTP mocks with RockTest

It is easy to create HTTP mocks to mimic a real API, and simulate errors.

## Parameters

### Global structure

|   Name   | Usage                                    | Type      | Optional |
| -------- | ---------------------------------------- | ----------|----------|
| desc     | Step description, for logs and report    | string    | Yes      |
| params   | Parameters (see below)                   | map       | No       |

### _params_ map

The _params_ structure is nested. Its structure is the following :

|   Name   | Usage                       | Type      | Optional |
| -------- | --------------------------- | ----------|----------|
| port     | Listen port                 | int       | No       |
| headers  | Default headers             | map       | No       |
| when     | List of condition map       | list      | No       |

### _condition_ entry

You can define multiple conditions. When a condition match, the result described in the condition is
returned by the mock.

As a YAML list, each entry starts with a dash ( - ).

|   Name   | Usage                                   | Type      | Optional |
| -------- | --------------------------------------- | ----------|----------|
| uri      | Matching URI (regex)                    | string    | No       |
| method   | HTTP method                             | string    | No       |
| call     | Module to call, with its parameters     | list      | Yes      |
| response | Response to send when condition matches | map       | No       |


### _response_ map

The reponse map contains the following entries :

|   Name   | Usage                                   | Type      | Optional |
| -------- | --------------------------------------- | ----------|----------|
| code     | HTTP code to send                       | int       | No       |
| body     | body content to send                    | string    | No       |


## Step template

````yaml
- step: http.mock
  params:
    port: <listen port>
    headers:
      header1: content1
      header2: content2
      # ...
    when:
      # Condition 1
      - uri: <URI regex>
        method: <method> # get, post, put, delete
        call:
          value: <function to call when condition is met>
          params:
            module_param1: module_value1
            module_param2: module_value2
            # ...
        response:
          code: <response code>
          body: <reponse body>
      # Condition 2
      # ...
````

## Actions

When the step is called, 

