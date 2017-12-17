(def VERSION (or (not-empty (System/getenv "SEMVER")) "0.0.1-SNAPSHOT"))

(defproject net.omnypay/nomad-migrators VERSION
  :encoding "utf-8"
  :description "OmnyPay Nomad migrators for H2, PostgreSQL and Datomic"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.443"]
                 [com.h2database/h2 "1.4.192"]
                 [net.omnypay/nomad "0.1.0"]
                 [org.clojure/java.jdbc "0.7.1"]
                 [org.postgresql/postgresql "42.1.4"]
                 [com.datomic/clj-client "0.8.606"]]
  :profiles {:dev {:dependencies
                   [[com.datomic/datomic-pro "0.9.5561.62"]]}})
