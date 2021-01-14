# Create HTTP mocks with RockTest

It is easy to create HTTP mocks to mimic a real API, and simulate errors.

## Sample scenarios


https://github.com/rockintest/rocktest/tree/main/docs/example/httpmock

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

### General algorithm

- When the step is called, a thread is created, and it starts to listen to the port _params.port_. Then clients can start using the mock.
- When a client connects to the mock, the URI and method of the request are checked against the conditions, in the order
of the scenario. 
  - If no condition matches, the mock generates a 404 response with a basic body
  - If a condition matches, the entry of the _when_ list is considered :
    - If the parameter _call_ exists, the according sub-scenario is called, with the parameters, if any
    - Then the response is sent to the client, with the code and body specified in the condition. The header
      in the response contain the default headers, specified in _params.headers_, and the headers specified in the condition,
      if any.

---
#### Example : simple HTTP mock

````yaml
- step: http.mock
  params:
    port: 8080
    # Default header. Always set.
    headers:
      content-type: application/json
    when:
      - uri: /singer/springsteen
        method: get
        # Response to send when condition is met
        response:
          code: 200
          body: >-
            {
              "fullname": "Bruce Springsteen aka The Boss",
              "bestsong": "Born in the USA"
            }

- step: pause forever
````


When the mock is called with the "/singer/springsteen" URI avd the GET method, the static JSON is
returned in the body :

````json
{
    "fullname": "Bruce Springsteen aka The Boss",
    "bestsong": "Born in the USA"
}
````

You can add other conditions, matching other URI and methods :

````yaml
- step: http.mock
  params:
    port: 8080
    # Default header. Always set.
    headers:
      content-type: application/json
    when:
      - uri: /singer/springsteen
        method: get
        # Response to send when condition is met
        response:
          code: 200
          body: >-
            {
              "fullname": "Bruce Springsteen aka The Boss",
              "bestsong": "Born in the USA"
            }
            
      - uri: /singer/jagger
        method: get
        # Response to send when condition is met
        response:
          code: 200
          body: >-
            {
              "fullname": "Mick Jagger and the Rolling Stones",
              "bestsong": "Satisfaction"
            }

- step: pause forever
````


### Extract path variables

In the _when.uri_ regex, it is possible to use regex extractions. The groups are available as ${1}...${n}
variables.
Those variables can be passed in the returned body.

````yaml
- step: http.mock
  params:
    port: 8080
    headers:
      content-type: application/json
    when:
      - uri: /singer/([^/]*)/([^/]*).*
        method: get
        response:
          code: 200
          body: >-
            {
              "band": "${1}",
              "name": "${2}"
            }
- step: pause forever
````

If we call this mock with the following URL :

    http://localhost:8080/singer/inxs/hutchence/mickael

The result will be :

````json
{
    "band": "inxs",
    "name": "huthence"
}
````

The first group in the regex matches "inxs" and the second "hutchence".

### Call a logic

If there is a _call_ parameter in the condition, the corresponding function is called. 

`Note`: the function can be in another scenario.

````yaml
- step: http.mock
  params:
    port: 8080
    headers:
      content-type: application/json
    when:
      - uri: /singer/([^/]*).*
        method: get
        call:
          value: -> onSinger
          params:
            singer: ${1}
        response:
          code: 200
          body: >-
            {
              "singer": "${1}"
            }
- step: pause forever

# Defines the logic
- step: function onSinger
  steps:
    - step: display The singer is ${singer}
````

When the mock is called on the URL

    http://localhost:8080/singer/inxs

the variable "inxs" is extracted and put in ${1}. Then the function _onSinger_ is called, and the _singer_
parameter is set to the extracted value (here "inxs").

