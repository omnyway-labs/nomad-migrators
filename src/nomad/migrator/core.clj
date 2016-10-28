(ns nomad.migrator.core
  (:require
   [clojure.java.jdbc :as jdbc])
  (:import
   [java.util Date]
   [java.sql Timestamp]))

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
  (jdbc/with-connection db-spec
    (jdbc/with-query-results rset
      ["SELECT * FROM nomad_schema_migrations"]
      (->> rset
           (map #(keyword (:tag %)))
           doall))))

(defn applied? [{:keys [db-spec]} tag]
  (jdbc/with-connection db-spec
    (jdbc/with-query-results rset
      ["SELECT * FROM nomad_schema_migrations WHERE tag=?" (name tag)]
      (-> (doall rset) count pos?))))

(defn apply! [{:keys [db-spec]} tag migration-fn]
  (jdbc/with-connection db-spec
    (jdbc/transaction
     (migration-fn)
     (jdbc/insert-record :nomad_schema_migrations
                         {:tag (name tag)
                          :applied (date->timestamp
                                    (java.util.Date.))}))))

(defn fini [{:keys [db-spec]}]
  )
