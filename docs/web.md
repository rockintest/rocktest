# Web module

This module allow to automate Web test, by driving a browser using Selenium.

### Element selector

Most of the functions need to locate an element in the web page.  

RockTest uses Selenium under the hood to automate browser testing. That's why you should read the Selenium
documentation too.

This link points to the Selenium in Python, but the concepts are the same : https://selenium-python.readthedocs.io/locating-elements.html
The present documentation is stongly inspired by the Python doc. Thanks Selenium/Python guys. You rock !

Useful resources :

- [Selenium Python](https://selenium-python.readthedocs.io/locating-elements.html)
- [Selenium WebDriver documentation](https://www.selenium.dev/documentation/fr/webdriver/locating_elements/)
- [The WebDriver API](https://www.selenium.dev/documentation/en/webdriver/web_element/)
- [Selenium locators](https://www.guru99.com/locators-in-selenium-ide.html)
- [Selenium tutorials](https://www.guru99.com/selenium-tutorial.html)

#### Parameters 

##### Selector

|   Name         | Usage                                        | Type      | Optional | Default value   |
| -------------- | -------------------------------------------- | ----------|----------|-----------------|
| by.*           | The selector (see below)                     | String    | No       |                 |
| from           | The element to search from (in the DOM tree) | selector  | Yes      | null (unset)    |
| order          | Order of the element                         | int       | Yes      | 1               |
| wait           | Time (in seconds) to wait for the element    | int       | Yes      | 10 seconds      |

- from: another selector. If it is set, the search starts from this element. 
- order: if the selector returns multiple elements (e.g if you select all the divs of the page), this parameter is the index of the returned element. 
- wait: RockTest automatically waits for the element. You can adjust the maximum allowed time, which is 10 seconds by default. If the element is not present after the timeout, the scenario fails.


##### By.linktext : Locating Hyperlinks by Link Text

Use this when you know the link text used within an anchor tag. With this strategy, the _order_<sup>th</sup> element with the link text matching the provided value will be returned.

|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.linktext    | Value of the link to search                      | String    | No       |                 |
| exact          | Search for the exact string or a substring       | boolean   | Yes      | false           |

For instance, consider this page source:

````html
<html>
 <body>
  <p>Are you sure you want to do this?</p>
  <a href="continue.html">Continue</a>
  <a href="cancel.html">Cancel</a>
</body>
<html>
````

The continue.html link can be located like this:

````yaml
by.linktext: Continue
exact: true
````
 or

````yaml
by.linktext: Conti
# exact: false => can be ommited since false is the default 
````

##### By.content : Locating element by its content

Use this when you know the content of the element. With this strategy, the _order_<sup>th</sup> element with the content matching the provided value will be returned (substring match).

|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.content     | Content to search                                | String    | No       |                 |

For instance, consider this page source:

````html
<html>
 <body>
  <p>Are you sure you want to do this?</p>
  <a href="continue.html">Continue</a>
  <a href="cancel.html">Cancel</a>
</body>
<html>
````

The <p> element can be located like this:

````yaml
by.content: Are you sure
````

##### By.name : Locating element by name

Use this when you know the name attribute of an element. With this strategy, the _order_<sup>th</sup> element with a matching name attribute will be returned.

|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.name        | Name to search                                   | String    | No       |                 |

For instance, consider this page source:

````html
<html>
 <body>
  <form id="loginForm">
   <input name="username" type="text" />
   <input name="password" type="password" />
   <input name="continue" type="submit" value="Login" />
   <input name="continue" type="button" value="Clear" />
  </form>
</body>
<html>
````

The username & password elements can be located like this:

````yaml
by.name: username
````

````yaml
by.name: password
````

This will give the “Login” button as it occurs before the “Clear” button:

````yaml
by.name: continue
````

To select the "Clear" button, use the _order_ attribute :

````yaml
by.name: continue
order: 2
````

##### By.class : Locating Elements by Class Name

Use this when you want to locate an element by class name. With this strategy, the _order_<sup>th</sup> element with the matching class name attribute will be returned.

|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.class       | Class to search                                  | String    | No       |                 |

For instance, consider this page source:

````html
<html>
<body>
<p class="content">Site content goes here.</p>
</body>
<html>
````

The “p” element can be located like this:

````yaml
by.class: content
````

##### By.css : Locating Elements by CSS Selectors

Use this when you want to locate an element using [CSS selector](https://developer.mozilla.org/en-US/docs/Learn/CSS/Building_blocks/Selectors) syntax. With this strategy, the _order_<sup>th</sup> element matching the given CSS selector will be returned.

|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.css         | CSS selector expression                          | String    | No       |                 |

For instance, consider this page source:

````html
<html>
<body>
<p class="content">Site content goes here.</p>
</body>
<html>
````

The “p” element can be located like this:

````yaml
by.class: content
````

[Sauce Labs has good documentation](https://saucelabs.com/resources/articles/selenium-tips-css-selectors) on CSS selectors.


##### By.id : Locating Elements by Id

Use this when you know the id attribute of an element. With this strategy, the _order_<sup>th</sup> element with a matching id attribute will be returned.

|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.id          | Id of the element to search                      | String    | No       |                 |

For instance, consider this page source:

````html
<html>
<body>
<form id="loginForm">
    <input name="username" type="text" />
    <input name="password" type="password" />
    <input name="continue" type="submit" value="Login" />
</form>
</body>
<html>

````

The “form” element can be located like this:

````yaml
by.id: loginForm
````

##### By.tag : Locating Elements by Tag Name

Use this when you want to locate an element by tag name. With this strategy, the _order_<sup>th</sup> element with the given tag name will be returned.

|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.tag         | tag of the element to search                     | String    | No       |                 |

For instance, consider this page source:

````html
<html>
<body>
<h1>Welcome</h1>
<p>Site content goes here.</p>
</body>
<html>

````

The heading (h1) element can be located like this:

````yaml
by.tag: h1
````

##### By.xpath : Locating Elements by XPath

XPath is the language used for locating nodes in an XML document. As HTML can be an implementation of XML (XHTML), Selenium users can leverage this powerful language to target elements in their web applications. XPath supports the simple methods of locating by id or name attributes and extends them by opening up all sorts of new possibilities such as locating the third checkbox on the page.

One of the main reasons for using XPath is when you don’t have a suitable id or name attribute for the element you wish to locate. You can use XPath to either locate the element in absolute terms (not advised), or relative to an element that does have an id or name attribute. XPath locators can also be used to specify elements via attributes other than id and name.

Absolute XPaths contain the location of all elements from the root (html) and as a result are likely to fail with only the slightest adjustment to the application. By finding a nearby element with an id or name attribute (ideally a parent element) you can locate your target element based on the relationship. This is much less likely to change and can make your tests more robust.


|   Name         | Usage                                            | Type      | Optional | Default value   |
| -------------- | ------------------------------------------------ | ----------|----------|-----------------|
| by.xpath       | XPath expression                                 | String    | No       |                 |


For instance, consider this page source:

````html
<html>
<body>
<form id="loginForm">
    <input name="username" type="text" />
    <input name="password" type="password" />
    <input name="continue" type="submit" value="Login" />
    <input name="continue" type="button" value="Clear" />
</form>
</body>
<html>
````

The form elements can be located like this:

````yaml
by.xpath: /html/body/form[1]
````

In order to learn more, the following references are recommended:

- [W3Schools XPath Tutorial](https://www.w3schools.com/xml/xpath_intro.asp)
- [W3C XPath Recommendation](http://www.w3.org/TR/xpath)
- [XPath Tutorial](http://www.zvon.org/comp/r/tut-XPath_1.html) - with interactive examples.

#### Example using from

Consider this source code :

````html
<html>
    <h1>Test</h1>
    <table>
        <tr>
            <td>L1C1</td>
            <td>L1C2</td>
            <td>L1C3</td>
        </tr>
        <tr>
            <td>L2C1</td>
            <td>L2C2</td>
            <td>L2C3</td>
        </tr>
        <tr>
            <td>L3C1</td>
            <td>L3C2</td>
            <td>L3C3</td>
        </tr>
        <tr>
            <td>L4C1</td>
            <td>L4C2</td>
            <td>L4C3</td>
        </tr>
    </table>
</html>
````

You can locate the Line 3 / Column 2 of the table :

````yaml
# Nested from
- step: web.text
  params:
    by.tag: td
    order: 2
    from:
      by.tag: tr
      order: 3
      from:
        by.tag: table
````

RockTest will first locate the deepest _from_, which is

````yaml
      from:
        by.tag: table
````

From this element, it locates the 3rd row :

````yaml
# Nested from
    from:
      by.tag: tr
      order: 3
      from:
        by.tag: table
````

Finally, in this locates the 2nd column, from this row.


### *web.get* : open an URL

---

#### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| url            | The URL to open                          | string    | No       |                         |
| browser        | The browser to use                       | string    | Yes      | firefox                 |
| browserOptions | Options for the browser                  | string    | Yes      | null (empty)            |


- browser : firefox | chrome
- browserOptions : depend on the browser. A useful option (common to Firefox and Chrome is "--headless" to open the browser in headless mode: no visible window)

#### Template

```yaml
- web.get:
  params:
    url: <the url>
    browser: <chrome | firefox> # Optional
    browserOptions: # Optional
    - <option1>
    - <option2>
```

#### Output variable

None

#### Actions

Runs a browser (Firefox or Chrome), and opens the specific URL.

##### Example

```yaml
- web.get:
  params:
    url: http://www.google.com
    browser: firefox
    browserOptions:
      - --headless
```

Opens Firefox in headless mode, and goes to www.google.com

### *web.hide* : hide browser window

---

#### Parameters

None

#### Template

```yaml
- web.hide:
```

#### Output variable

None

#### Actions

Hides the browser window (if not in headless mode)


##### Example

```yaml
- web.get:
  params:
    url: http://www.google.com
- pause: 2
- web.hide:
```

Opens the browser window and hides the window.

### *web.show* : show browser window

---

#### Parameters

None

#### Template

```yaml
- web.show:
```

#### Output variable

None

#### Actions

Shows the browser window (if not in headless mode)


##### Example

```yaml
- web.get:
  params:
    url: http://www.google.com
- pause: 2
- web.hide:
- pause: 2
- web.show:
```

Opens the browser window, hides the window and shows it again.

### *web.title* : get the title of the Browser Window 

---

#### Parameters

None

#### Template

```yaml
- web.title:
```

#### Output variable

    ${title.result}

#### Actions

Gets the title of the browser window and returns it in a variable.


##### Example

```yaml
- web.get:
  params:
    url: http://www.google.com

# Standard notation
- web.title:
- display: ${title.result}

# Compact notation
- display: ${$web.title()}
```

Opens the browser window, displays the window title.

`Note`: you can call the module in an expression (aka compact notation).


### *web.url* : get the current URL

---

#### Parameters

None

#### Template

```yaml
- web.url:
```

#### Output variable

    ${url.result}

#### Actions

Gets the current URL and returns it in a variable.


##### Example

```yaml
- web.get:
  params:
    url: http://www.google.com

# Standard notation
- web.url:
- display: ${url.result}

# Compact notation
- display: ${$web.url()}
```

Opens the browser window, displays the current URL.

`Note`: you can call the module in an expression (aka compact notation).

### *web.window* : get the current window handle

---

#### Parameters

None

#### Template

```yaml
- web.window:
```

#### Output variable

    ${window.result}

#### Actions

Gets current Window handle. Useful if the site has popups, or multiple windows, or iframes.

##### Example

```yaml
- web.get:
  params:
    url: http://www.google.com

# Standard notation
- web.window:
- display: ${window.result}

# Compact notation
- display: ${$web.window()}
```

Opens the browser window, displays the current window.

`Note`: you can call the module in an expression (aka compact notation).


### *web.newwindow* : get the last created window handle

---

If the last action opened a new window (for example a click on a button opens another tab), 
the handle of this window can be retreived using wbe web.newwindow module.

#### Parameters

None

#### Template

```yaml
- web.newwindow:
```

#### Output variable

    ${newwindow.result}

#### Actions

Gets Window handle of the last created window. Useful if the site has popups, or multiple windows, or iframes.

##### Example

```yaml
- web.get:
  params:
    url: https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_win_open

# Displays the current window handle
- display: ${$web.window()}

# A popup appears to accept the cookies, click accept
- web.click:
  params:
    by.id: accept-choices

# Switch to the frame on the right side
- web.switch:
  params:
    frame:
      id: iframeResult

# Click the button which opens a new window
- web.click:
  params:
    by.content: Try it

# Display the new window handle
- display: ${$web.newwindow()}

- pause: 5
```

Opens the browser window, displays the current window.

`Note`: you can call the module in an expression (aka compact notation).


### *web.switch* : switch to a window or iframe

---

When the application opens new windows, you can use the `web.switch` module to
switch between those elements.

You can switch to :
- another frame or iframe
- another window
- the default window (switch back to the main window)

#### Switch to another window

To switch to another window, pass its handle using the "window" parameter.

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| window         | Window to switch to                      | string    |          |                         |

##### Template

````yaml
- web.switch:
  params:
    window: <window handle>
````

#### Switch to another frame

To switch to another frame, use the "frame" param, which is actually a map with 1 of the 3 elements:

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| frame.id       | id of the frame                          | string    |          |                         |
| frame.index    | index of the frame                       | int       |          |                         |
| frame.name     | name of the frame                        | name      |          |                         |
| frame.by.*     | selector of the frame                    | selector  |          |                         |


- If the frame has an id, you can use it to switch to it.
- Else, use _index_ to switch to the index<sup>th</sup> frame 
- Or, use _name_ if the frame has a name
- Finally, you can use any selector to locate the frame (by.* param) see [Selector](#selector) 

##### Templates

<u>Using id</u>

````yaml
- web.switch:
  params:
    frame: 
      id: <the frame id>
````

##### Example

````yaml
- web.get:
  params:
    url: https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_win_open

# Switch to the frame on the right side
- web.switch:
  params:
    frame:
      id: iframeResult
````

<u>Using index</u>

````yaml
- web.switch:
  params:
    frame: 
      index: <the frame index>
````

<u>Using name</u>

````yaml
- web.switch:
  params:
    frame: 
      name: <the frame name>
````

<u>Using a selector</u>

You can locate the frame using any selector. The result of the search must be the frame
to switch to.

````yaml
- web.switch:
  params:
    frame: 
      by.tag: iframe
````


### *web.attribute* : get an attribute of a web element

---

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector)                    | selector  | No       |                         |
| name           | Attribute to fetch                       | string    | No       |                         |

##### Output

The result is returned in the following variable :

    ${attribute.value}

##### Example

There is the HTML document :

````html
<html>
   <h1 name="title" customAttribute="customValue">Test</h1>
</html>
````

To get the value of the attribute `customAttribute` of the \<h1\> use the following :

````yaml
- web.attribute:
  params:
    # Seletor => select the first element with tag name <h1>
    by.tag: h1
    # Read the content of the attrinute "customAttribute"
    name: customAttribute

- display: attribute value=${attribute.result}
# Displays "customValue"
````


### *web.css* : get a CSS attribute of a web element

---

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector)                    | selector  | No       |                         |
| name           | CSS Attribute to fetch                   | string    | No       |                         |

##### Output

The result is returned in the following variable :

    ${css.value}

##### Example

There is the HTML document :

````html
<html>
   <h1 style="font-size:12px">Test</h1>
</html>
````

To get the value of the CSS attribute `font-size` of the \<h1\> use the following :

````yaml
- web.css:
  params:
    # Seletor => select the first element with tag name <h1>
    by.tag: h1
    # Read the content of the CSS attrinute "font-size"
    name: font-size

- display: CSS attribute value=${css.result}
# Displays "12px"
````


### *web.tag* : get the tag name of a web element

---

This module returns the tag name of the selected element.

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector)                    | selector  | No       |                         |

##### Output

The result is returned in the following variable :

    ${tag.value}

##### Example

There is the HTML document :

````html
<html>
  <h1 id="myh1">Test</h1>
</html>
````

To get the value of the tag name of the element with id "myh1" :

````yaml
- web.tag:
  params:
    # Selects the element by ID
    by.id: myh1

- display: tag value=${tag.result}
# Displays "h1"
````



### *web.text* : get the text of a web element

---

This module returns the text visible in the browser for a specific element.

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector)                    | selector  | No       |                         |

##### Output

The result is returned in the following variable :

    ${text.value}

##### Example

There is the HTML document :

````html
<html>
    <div id="myh1">
        <h1>Title</h1>
        <h2>Subtitle</h2>
    </div>
</html>

````

To get the text of the div with id "myh1" :

````yaml
- web.text:
  params:
    by.id: myh1
````

The text of this element is :

````
Title
Subtitle
````

To check its content in RockTest, you can use this YAML :

````yaml
- assert.equals:
  params:
    expected: |-
      Title
      Subtitle
    actual: ${text.result}
````

Note the use of " |- " in YAML which allows to set values with newlines.


### *web.submit* : submits a form

---

Call web.submit on any element in a \<form\> to submit the form

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector)                    | selector  | No       |                         |

The selector must point to any element inside a form.

##### Output

None

##### Example

There is the HTML document :

````html
<html>
    <body>
        <form action="/submit">
            <label for="fname">First name:</label><br>
            <input type="text" id="fname" name="fname" value="Rock"><br>
            <label for="lname">Last name:</label><br>
            <input type="text" id="lname" name="lname" value="Test"><br><br>
            <input type="submit" value="Submit">
        </form>
    </body>
</html>
````

To submit this form, use the following RockTest yaml :

````yaml
- web.submit:
  params:
    by.id: fname
````

`Note`: you could use any element in the form :

This locates the second field :

````yaml
- web.submit:
  params:
    by.id: lname
````

Or, to select the button (which is the 3rd element with an input tag) :
keys
````yaml
- web.submit:
  params:
  by.tag: input
  order: 3
````

### *web.sendkeys* : send a string to an element

---

This module allows to enter text in the selected element.

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector) for the destination| selector  | No       |                         |
| value          | The string to send                       |           |          |                         |

##### Output

None

##### Example

There is the HTML document :

````html
<html>
  <body>
    <input type="text" value="Rock">
  </body>
</html>
````

To append "Test" to the input field use:

````yaml
- web.sendkeys:
  params:
    by.tag: input
    value: Test
````

To check its content in RockTest, you can use this YAML :

````yaml
- web.attribute:
  params:
    by.tag: input
    name: value

- display:  Value=${attribute.result}

- assert.equals:
  params:
    expected: RockTest
    actual: ${attribute.result}
````

Note that to get the content of an input, you cannot use web.text. Instead, read the
"value" property of the input, using web.attribute.


### *web.clear* : clears an element

---

This module allows to clear text in the selected element (if the element is writable)

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector) for the destination| selector  | No       |                         |

##### Output

None

##### Example

There is the HTML document :

````html
<html>
  <body>
    <input type="text" value="Rock">
  </body>
</html>
````

To remove the content of the input use:

````yaml
- web.clear:
  params:
    by.tag: input
````

### *web.count* : counts the number of element matching the selection

---

This module allows to get the number of element matching the selector. If no element 
is present, wait for the given timeout. If still no element are present after the
timeout, returns 0.

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector) for the elements to count | selector  | No       |                         |
| wait           | Maximum time to wait until returning 0   | int       | yes      | 10 

##### Output

    ${count.result}

##### Example

There is the HTML document :

````html
<html>
    <h1>Test</h1>
    <table id="t1">
        <tr>
            <td>L1C1</td>
            <td>L1C2</td>
            <td>L1C3</td>
        </tr>
    </table>
    <table id="t2">
        <tr>
            <td>L1C1</td>
            <td>L1C2</td>
            <td>L1C3</td>
        </tr>
        <tr>
            <td>L2C1</td>
            <td>L2C2</td>
            <td>L2C3</td>
        </tr>
    </table>
</html>
````

To count the total number of `td` element use :

````yaml
- web.count:
  params:
    by.tag: td

- display: There are ${count.result} TD in all the document
````

You can count the number of element in a subtree of your DOM, using a `from` clause in the selector.

In the above example, to count the number of rows in the table with ID _t1_, use the following yaml :

````yaml
- web.count:
  params:
    by.tag: tr
    from:
      by.id: t1

- display: There are ${count.result} rows in the first table
````

### *web.click* : sends a click

---

Click on the element located.

##### Parameters

|   Name         | Usage                                    | Type      | Optional | Default value           |
| -------------- | ---------------------------------------- | ----------|----------|-------------------------|
| by.* + from    | [Selector](#selector) for the element to be clicked | selector  | No       |                         |

##### Output

None.

##### Example

There is the HTML document :

````html
<html>
<body>
  <button onclick="myFunction()">Click me if you rock !</button>
  <p id="rock"></p>

  <script>
    function myFunction() {
    document.getElementById("rock").innerHTML = "RockTest";
  }
  </script>
</body>
</html>
````

This scenario clicks on the button and verifies that the \<p> has been correctly updated: 

````yaml
- web.get:
  params:
  url: 'http://localhost:8080/click'

- web.click:
  params:
  by.tag: button

- web.text:
  params:
  by.id: rock

- assert.equals:
  params:
  expected: RockTest
  actual: ${text.result}
````


### *web.quit* : terminates the session

---

Closes the browser and terminates the session.

##### Parameters

None.

##### Output

None.

##### Example

````yaml
web.quit:
````


### *web.search* : search elements

---



