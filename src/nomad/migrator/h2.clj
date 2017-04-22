(ns nomad.migrator.h2
  (:require
   [nomad.core :as nomad :refer [defmigration]]
   [nomad.migrator.core :as migrator]
   [clojure.java.jdbc :as jdbc]))

(defrecord H2Store [db-spec])

(defn connect [{:keys [db]}]
  (H2Store.
   {:subname db
    :subprotocol "h2"
    :classname "org.h2.Driver"}))

(defn init [{:keys [db-spec]}]
  (jdbc/with-db-transaction [db db-spec]
     (when (empty?
            (jdbc/query
             db
             [(str "SELECT * FROM information_schema.tables "
                   "WHERE table_schema = 'PUBLIC' "
                   " AND table_name = 'NOMAD_SCHEMA_MIGRATIONS'")]))
       (jdbc/db-do-commands
        db
        (str "CREATE TABLE nomad_schema_migrations"
             "(tag VARCHAR(256)"
             ",applied TIMESTAMP"
             ",CONSTRAINT uq_tag UNIQUE (tag))")))))

;; FIXME: add IMigrator protocol fn for list-all-tables
(defn list-all-tables [{:keys [db-spec]}]
  (jdbc/with-db-connection [db db-spec]
    (jdbc/query
     db
     [(str "SELECT * FROM information_schema.tables "
           "WHERE table_schema = 'PUBLIC'")])))

;; FIXME: add IMigrator protocol fn for drop-all-tables
(defn drop-all-tables [{:keys [db-spec]}]
  (jdbc/with-db-connection [db db-spec]
    (jdbc/query
     db
     [(str "SELECT * FROM information_schema.tables "
           "WHERE table_schema = 'PUBLIC'")]
     :row-fn (fn [{:keys [table_name]}]
               (println "DROP TABLE" table_name)
               (jdbc/db-do-commands
                db
                (str "DROP TABLE IF EXISTS " table_name " CASCADE"))))))

(extend H2Store
  nomad/IMigrator
  {:-init init
   :-fini migrator/fini
   :-load-migrations migrator/load-migrations
   :-applied? migrator/applied?
   :-apply! migrator/apply!})
