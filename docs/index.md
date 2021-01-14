# Welcome to RockTest 

## Getting started

To setup RockTest, please refer to [README](https://github.com/rockintest/rocktest/blob/main/README.md) on GitHub.

Sample "Hello RockTest" scenario, with a variable :

```yaml
- step: var rock = Hello RockTest
- step: display ${rock}
```

## Features

RockTest is a low code tool, designed to automate your tests. It's easy to use, and extensible.

<table style="border:none">
<tr>
<td>
- [YAML syntax](core.md), with the possibility to define functions and call sub scenarios
</td>
</tr>
</table>


- [YAML syntax](core.md), with the possibility to define functions and call sub scenarios
- [HTTP client](httpclient.md): you can call an HTTP server, with any HTTP method and check the result
- [HTTP mocks](httpmock.md): create mocks for your APIs in less than 20 lines of YAML. You can create a Mock and use it in the same RockTest !
- [SQL requests](sql.md): rock your SQL requests, extract data and check the results. Generate YAML DAO for your tables.
- [Web tests](web.md): open up your browser, and automate your end-to-end tests
- [Assertions](assert.md): do checks using exact match or regex
- [JSON](json.md): JSON is supported, with the use of JSONPath API
- [Date](date.md): manipulate dates, get current date and do calculations on dates
- [Id](id.md): generate IDs, based on sequences, with custom starting and increment, or UUIDs

All of these features can be combined in a single scenario. You can spin a Mock of your API, and do e2e tests
on your web application using this Mock. Or you can test your REST API by provisioning your database and calling 
your services.

If you need to [extend RockTest](extend.md), it is very easy too. Just create a Java class, extending the RockModule class. 
Then implement a function accepting a Map as parameter, and returning a map. Put this class in your classpath, no need to rebuild,
all is dynamic !

Happy Rock'in !
