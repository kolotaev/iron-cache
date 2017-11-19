# iron-cache

Currently WIP. Hold on... :)

A Clojure client for [Iron Cache](http://www.iron.io/cache).

## Usage

Alternatively you can call cache manipulation functions on a particular client:

```clojure
(:require '[iron-cache.core :as ic])

(let [client (ic/new-client {:project "my-project"})
      another-client (ic/new-client {:project "another-project"})]
      (ic/list client)
      (ic/info client :books)
      (ic/delete! another-client :orders)
      (ic/clear! client :users)
      (ic/get another-client :users :id)
      (ic/put client :users :name "Bob")
      (ic/incr another-client :users :salary 200)
      (ic/del client :users "Sally"))
```

## License

Copyright © 2017 Egor Kolotaev

Distributed under the Eclipse Public License 1.0.
