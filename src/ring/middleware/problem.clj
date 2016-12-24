(ns ring.middleware.problem
  ""
  (:require [cheshire.core :as json]
            [clojure.stacktrace :refer [print-stack-trace]]))

(defrecord Problem [type title status detail instance])

(defprotocol Problematic
  (problem [this]))

(defn +data
  "Add exception data to the problem. Useful for overriding
  default properties like status or adding instance specific
  properities like details."
  [problem exception]
  (merge problem (ex-data exception)))

(defn +uuid
  "Add a unique identifier (UUID) to `Problem`."
  [problem exception]
  (assoc problem :instance (java.util.UUID/randomUUID)))

(defn make-instance
  "Add set instance to a UUID and add ex-info data to `problem`."
  [problem exception]
  (-> problem
      (+uuid exception)
      (+data exception)))

(defn make-problem
  "Create a problem from spec.

  Specs have the following form:

  ```
  [exception-class attributes builder]
  ```

  If builder is omitted, then `make-instance` is used by
  default since the expected behavior of calling `(problem ex)`
  is an instance specific occurrence of a problem.

  ```
  (fn [base-problem exception] ...)
  ```"
  ([class attrs]
   (make-problem class attrs make-instance))
  ([class attrs builder]
   (let [base (map->Problem attrs)]
     (extend class
       Problematic
       {:problem (fn [ex] (builder base ex))})
     base)))

(defn make-problems
  "Create `Problem`s from specs."
  [specs]
  (->> specs
       (map #(apply make-problem %))
       (map (juxt :type identity))
       (into {})))

(defmacro defproblems
  "Create `Problem` for given problem-specs bound to name."
  [name problem-specs]
  `(def ~name (make-problems ~problem-specs)))

(defproblems default-problems
  [[java.lang.RuntimeException
    {:type "default-problem"
     :title "Default problem"
     :status 500}
    make-instance]])

(defn as-json
  "Produces a response map for problem. Supports only JSON at present."
  [problem]
  {:body (json/encode problem)
   :status (:status problem)
   :headers {"Content-Type" "application/problem+json"}})

(defn wrap-problem
  "Catches exceptions raised by subsequent handlers and produces
   problem response."
  ([handler]
   (wrap-problem handler identity))
  ([handler transform]
   (fn [request]
     (try
       (let [response (handler request)]
         response)
       (catch java.lang.RuntimeException ex
         (-> ex
             (problem)
             (transform request)
             (as-json)))))))
