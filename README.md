[![Build Status](https://travis-ci.org/kolotaev/iron-cache.svg?branch=master)](https://travis-ci.org/kolotaev/iron-cache)

# IronCache Client Library for Clojure

A Clojure client for [Iron Cache](http://www.iron.io).


## Artifact

Leiningen/Boot:

[![Clojars Project](http://clojars.org/iron-cache/latest-version.svg)](https://clojars.org/iron-cache)

Gradle:
```
compile "iron-cache:iron-cache:1.0.0"
```

Maven:
```xml
<dependency>
  <groupId>iron-cache</groupId>
  <artifactId>iron-cache</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Documentation

- [Prerequisites](#prerequisites)
- [Actions](#actions)
- [Configuration](#configuration)
- [Usage](#usage)
	- [Basic usage](#basic-usage)
	- [Global client](#global-client)
	- [With macro](#with-macro)
	- [Async](#async)
- [Development](#development)


### Prerequisites

Before you proceed with *iron-cache* client you have to set up your project and obtain OAuth token at [Iron.io](http://www.iron.io/).


### Actions

*iron-cache* client provides a set of actions that correspond to Iron Cache API [endpoints](http://dev.iron.io/cache/reference/api/#endpoints).

| Action      	|  Description   |
| ------------- |  ------------- |
| `list`		| Get a list of all caches in a project |
| `info`	    | Get information about a cache |
| `delete!`	    | Delete a cache and all items in it |
| `clear!`	    | Delete all items in a cache. This cannot be undone |
| `get`	        | Get a value stored in a key from a cache |
| `put`	        | Put an item with specific data into a cache |
| `incr`	    | Increments the numeric value of an item in a cache |
| `del`	        | Delete a value from a cache stored at key |


### Configuration

Client should be created with some configuration. Configuration is a simple map.

```clojure
  {
   :host "https://some.host" ; hostname of the Iron Cache service. Generally you do not want to specify it.
   :port 443 ; port of the Iron Cache service. Generally you do not want to specify it.
   :api_version 1 ; API version of the Iron Cache service. Generally you do not want to specify it.
   :parse-callbacks false ; If you want to use non-modified callbacks and manually parse response. See Async usage.
   :http-options {} ; A set of custom options for clj-http client. Optional.

   :project "foo" ; Project name. Required.
   :token "123-456-789" ; OAuth token. Required.
  }
```

As you can see `:project` and `:token` are required. Without it client creation will throw an error.
Optionally you can specify them in environment variables: _IRON_CACHE_PROJECT_ and _IRON_CACHE_TOKEN_.


### Usage

*iron-cache* client provides a variety of ways you can work with it. See below.


#### Basic usage

The preferred way of using *iron-cache* client is to instantiate a client (or a number of clients) and call functions
on them.

The arguments to functions are:
* Client instance - required
* Cache name      - required
* Key name        - for key operations
* Data            - map for key `put`, Numeric for key `incr`. See [put](http://dev.iron.io/cache/reference/api/#put_an_item_into_a_cache) and [incr](http://dev.iron.io/cache/reference/api/#increment_an_items_value)
* Callbacks       - map of :ok and :fail callbacks. See [async](#async)

Keys and cache names can be either strings or keywords.

```clojure
(require '[iron-cache.core :as ic])

(def client (ic/new-client {:project "bob-project", :token "123-456-789"}))
(def another-client (ic/new-client {:project "alice-project", :token "asdf-qwerty"}))

(ic/list client)
; ({:project_id "bob-project", :name "users"}, {:project_id "bob-project", :name "books"})

(ic/info client :books)
; {:size 85000}

(ic/delete! another-client "orders")
; {:msg "Deleted"}

(ic/clear! client :users)
; {:msg "Cleared"}

(ic/get another-client :users :john)
; {:cache "users", :key "john", :value {:name "John", :age 25}, :cas 12345}

(ic/put client :users :bob-id {:value {:name "Bob", :phone 555-89-78}, "expires_in" 456, :replace true})
; {:msg "Stored"}

(ic/incr another-client :salaries :bob-id 200)
; {:msg "Added", :value 700}

(ic/del client :users "Sally")
; {:msg "Deleted"}
```

For unsuccessful requests (other than those returning 2XX) you will get a map:
```clojure
{
  :msg "Some description of the failure"
  :status 500
}
```

#### Global client

Alternatively you can initialize a global client and use all the functions with it implicitly.
You should use `iron-cache.global` namespace for that.

```clojure
(require '[iron-cache.global :as ic])

(init-client! {:project "amiga" :token "my-token"})

(ic/list)
; ({:project_id "bob-project", :name "users"}, {:project_id "bob-project", :name "books"})

(ic/info :books)
; {:size 85000}
```

#### With macro

We provide a handy macro for working with a client: `with-client`.
You should use `iron-cache.global` namespace for that.

```clojure
(require '[iron-cache.global :as ic])

(ic/with-client {:project "amiga" :token "my-token"}
  (ic/get :users :john)
  (ic/get :users :marry)
  (ic/get :users :sally))
```

Or

```clojure
(require '[iron-cache.global :as ic])
(require '[iron-cache.core :refer [new-client]])

(def client (new-client {:project "bob-project", :token "123-456-789"}))

(ic/with-client client
  (ic/get :users :john)
  (ic/get :users :marry)
  (ic/get :users :sally))
```

#### Async

All the calls are synchronous by nature. But, if you wish it's possible to make async calls simply by providing
a map with `:ok` and `:fail` callbacks. _At least one of them should be specified_.

All the responses are processed before their result is passed to a callback so that results conform those of
a synchronous calls. If you want to solely take response outputs and process them manually, set `:parse-callbacks false`
in client's configuration.

```clojure
(require '[iron-cache.core :as ic])

(def client (ic/new-client)) ; Project and token are taken from env
(def result (promise))

(ic/del client :sports :football {:ok #(deliver result %)
                                  :fail #(deliver result %)})
; some immediate other code here.
; ...
; and finally:
@result
```

### Development

Client has unit and integration tests. You can run them simply as
```bash
lein test :unit             # only unit-tests
lein test :integration      # only integration tests
lein test                   # all the tests
```

## License

Copyright © 2017 Egor Kolotaev.

Distributed under the Eclipse Public License 1.0.
