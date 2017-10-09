(ns nomad.migrator.athena
  (:require
   [nomad.core :as nomad :refer [defmigration]]
   [nomad.migrator.core :as migrator]))

(defrecord AthenaStore [db-spec])

(defn connect [{:keys [region] :as db-spec}]
  (AthenaStore.
   (merge db-spec {:subname (format "//athena.%s.amazonaws.com:443" region)
                   :subprotocol "awsathena"
                   :classname "com.amazonaws.athena.jdbc.AthenaDriver"})))

(defn apply! [{:keys [db-spec] :as store} tag migration-fn]
  (migration-fn store))

(extend AthenaStore
  nomad/IMigrator
  {:-init (constantly nil)
   :-load-migrations (constantly nil)
   :-fini migrator/fini
   :-applied? (constantly false)
   :-apply! apply!})
