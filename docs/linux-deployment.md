# Linux Deployment

## Prerequisites

- Use a Linux server with Docker Engine and Docker Compose v2.
- Allow outbound HTTPS traffic for the occupancy endpoint.
- Allow outbound PostgreSQL image and application image downloads during the build.
- Keep port `8080` closed on the public firewall.

The Compose file binds the application to `127.0.0.1` only. A local reverse proxy or Cloudflare Tunnel can reach this address.

## Install the application

1. Clone the repository on the server.
2. Change to the repository directory.
3. Copy `.env.example` to `.env`.
4. Set `POSTGRES_PASSWORD`.
5. Set `OCCUPANCY_API_URL`.
6. Run `docker compose up --build -d`.
7. Run `docker compose ps`.
8. Run `curl http://127.0.0.1:8080/actuator/health/liveness`.
9. Wait for one successful collection.
10. Run `curl http://127.0.0.1:8080/actuator/health/readiness`.

The liveness response must be `UP`. The readiness response becomes `UP` after PostgreSQL is available and a recent measurement exists.

## Update the application

1. Run `git pull --ff-only`.
2. Run `docker compose build --pull`.
3. Run `docker compose up -d`.
4. Run `docker compose ps`.
5. Check the readiness endpoint.

Docker Compose replaces the application container. The named PostgreSQL volume remains attached.

## View logs

Run this command:

```bash
docker compose logs --tail=200 app
```

Follow new log messages with this command:

```bash
docker compose logs --follow app
```

Normal collection creates one information message. A failed collection creates one warning message.

## Back up PostgreSQL

1. Create a directory that is not inside the repository.
2. Run this command:

   ```bash
   docker compose exec -T database pg_dump -U gym_occupancy -d gym_occupancy -Fc > gym-occupancy.dump
   ```

3. Copy `gym-occupancy.dump` to a second storage system.
4. Test the backup in a separate PostgreSQL database.

## Restore PostgreSQL

Warning: A restore changes database contents. Stop the application before the restore.

1. Run `docker compose stop app`.
2. Make a backup of the current database.
3. Run this command:

   ```bash
   docker compose exec -T database pg_restore --clean --if-exists -U gym_occupancy -d gym_occupancy < gym-occupancy.dump
   ```

4. Run `docker compose start app`.
5. Check the readiness endpoint.

## Troubleshooting

### The application container is unhealthy

1. Run `docker compose logs --tail=200 app`.
2. Check `OCCUPANCY_API_URL` in `.env`.
3. Check the upstream endpoint from the server.
4. Run `docker compose exec app curl --fail http://localhost:8080/actuator/health/readiness`.
5. Inspect the `db` and `occupancyData` components in the response.

### PostgreSQL is unhealthy

1. Run `docker compose logs --tail=200 database`.
2. Check available disk space.
3. Check `POSTGRES_PASSWORD` in `.env`.
4. Do not delete the PostgreSQL volume during troubleshooting.
