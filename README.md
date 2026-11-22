# MusicRanker — Phase 3

Java 17+ / Maven / SQLite terminal music ranking application.

## Run

```powershell
mvn clean compile
mvn exec:java
```

The database is automatically created at `data/musicranker.db` relative to the working directory. It is ignored by Git.

The app asks eight weighted 1–10 questions, supports adding, viewing, searching, editing, deleting and filtering songs, and saves to SQLite after changes.

**Note:** This version uses an atomic full-library save on every modification, suitable for a small personal library. A later version can switch to stable song IDs and individual SQL operations. If a database save fails, the app reports it and prevents normal exit until a save succeeds. Avoid force-closing before resolving the error.

**Existing database:** If you already created a `data/musicranker.db` with a different schema, back it up before using this project.
