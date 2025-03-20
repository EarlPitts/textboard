reset:
	rm textboard.db
	sqlite3 textboard.db < modules/core/src/main/resources/tables.sql
