(ns nomad.migrator.core
  (:require
   [clojure.java.jdbc :as jdbc])
  (:import
   [java.util Date]
   [java.sql Timestamp]))

(def ^:dynamic *current-conection* nil)

(defn current-connection []
  *current-connection*)

(defn as-timestamp
  ([]
   (as-timestamp (System/currentTimeMillis)))
  ([tstamp-ms]
   (Timestamp. tstamp-ms)))

(defn timestamp->date [x]
  (when x
    (-> x .getTime java.util.Date.)))

(defn date->timestamp [x]
  (when x
    (-> x .getTime as-timestamp)))

(defn load-migrations [{:keys [db-spec]}]
  (jdbc/with-db-connection [db db-spec]
    (jdbc/query
     db
     ["SELECT * FROM nomad_schema_migrations"]
     {:row-fn #(keyword (:tag %))})))

(defn applied? [{:keys [db-spec]} tag]
  (jdbc/with-db-connection [db db-spec]
    (-> (jdbc/query
         db
         ["SELECT * FROM nomad_schema_migrations WHERE tag=?" (name tag)])
        count
        pos?)))

(defn apply! [{:keys [db-spec]} tag migration-fn]
  (jdbc/with-db-transaction [db db-spec]
    (migration-fn)
    (jdbc/insert! db :nomad_schema_migrations
                  {:tag     (name tag)
                   :applied (date->timestamp
                             (java.util.Date.))})))

(defn fini [{:keys [db-spec]}])
