# ring-problem

Ring middleware for turning exceptions into RFC-7807 responses.

## Installation

### Leiningen

Include this in project.clj...

```
:dependencies [[jmorton/ring-problem "0.1.0-SNAPSHOT"]]
```

...and run...

```
lein deps
```

## Basic Usage

Require `ring.middleware.problem` in your app namespace and
add `problem/wrap-problem` to your handler middleware. Exceptions
that inherit from `java.lang.RuntimeException` will now be handled
and represented as an `application/problem+json` response.

```
(ns my.app
  (:require [ring.middleware.problem :as problem]))
(defn app [req] {:body "Hello, world."})
(defn app+middleware (-> app (problem/wrap-problem)))
```

Even though this works, it's likely you'll want to do more than
just use the default wrapper.

## Inevitable (Advanced) Usage

`ring-problem` works with minimal configuration but provides ways
to:

* define your own exception-problem mapping
* work with problems before responding with JSON

### Defining Problems

By default, `ring-problem` defines a single problem, one for
`java.lang.RuntimeException` to make it easy to get started.

```
(defproblems default-problems
  [[java.lang.RuntimeException
    {:type "default-problem"
     :title "Default problem type"
     :status 500}
    make-instance]])
```

You can override the default problem and provide your own using
the `defproblems` macro (or if you don't want to bind the resulting
map to a `Var`, `make-problems`).

```
(defproblems my-problems
  [[java.lang.RuntimeException
    {:type "my-default-problem"
     :title "My default problem type"
     :more "It was *your* mistake, so... 400."
     :status 400}]
   [my.app.DatabaseException
    {:type "my-database-vanished"
     :title "The database is currently unavailable"
     :status 500}]])
```

### Building Your Own Problem Instance

By default, calling `(problem ex)` will set the `instance` property
to a UUID and merge the map produced by `(ex-data ex)`. If you do
not want this behavior, you can pass a third option to problem-specs
in `defproblems`.

This is just an example, you probably don't want to do this.
p
```
(defproblems my-special-problems
  [[java.lang.RuntimeException
    {:type "my-default-problem"
     :title "My default problem type"
     :status 500}
    (fn [base ex]
      (assoc base :identity (fn [_] (java.lang.Math/random))))]])
```

### Saving Problems

First, define a function that takes a problem and returns it. The
function needs to return a problem because the middleware

```
(defn save
  "Persist instance of a problem for subsequent retrieval."
  [problem]
  (log/debug "Save problem to db")
  (db/save problem)
  problem)
```

Next, pass the saving function as an argument to `wrap-problem`.

```
(-> handler
    #_"more middleware"
    (problem/wrap-problem save)
    #_"more middlware")
```

This is a *really crude* example, hopefully you're passing the db
component to your save function as a parameter too!

### Providing a Problem Resource

You can also provide problems as a resource. Here is an example
of how you can do this if you're using `compojure`.

```
(defn problem-resource
  "Handlers for problem resource"
  [db msg]
  (context "/problems" req
   (GET "/" [] {:body (problem/as-json (get-problem request) problem/default-problems)}))))))
```

_Note: It's unlikely that support for this will be added by default because persistence and retrieval can vary widely between applications._

### TODO

* Consider support for [Slingshot](https://github.com/scgilardi/slingshot).
* Consider support for application/problem+xml representation.
* Consider support for logging.
* Summarize [RFC7807](https://tools.ietf.org/html/rfc7807)


## License

Copyright © 2016 Jonathan Morton

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
