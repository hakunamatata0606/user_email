migrate_up:
	migrate -path migration/ -database "mysql://root:123@tcp(127.0.0.1:3306)/user_email" -verbose up
migrate_down:
	migrate -path migration/ -database "mysql://root:123@tcp(127.0.0.1:3306)/user_email" -verbose down

.PHONY: migrate_up migrate_down