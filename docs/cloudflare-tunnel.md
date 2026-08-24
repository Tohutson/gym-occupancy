# Cloudflare Tunnel Deployment

This procedure uses a remotely managed Cloudflare Tunnel. The `cloudflared` service runs on the Linux host. The application remains bound to `127.0.0.1:8080`.

## Prerequisites

- Add the public domain to Cloudflare.
- Start the application with Docker Compose.
- Verify `http://127.0.0.1:8080/actuator/health/liveness` on the Linux host.
- Permit outbound traffic to Cloudflare. A restrictive firewall must permit tunnel traffic on port `7844`.

## Create the tunnel

1. Open the Cloudflare dashboard.
2. Go to **Networking > Tunnels**.
3. Select **Create a tunnel**.
4. Enter a name for the tunnel.
5. Select the Linux operating system and architecture.
6. Copy the installation command from the dashboard.
7. Run the installation command on the Linux host.
8. Wait until the tunnel status is healthy.

The installation command contains a tunnel token. Treat the token as a secret. Do not store the token in this repository.

## Publish the application

1. Open the tunnel in the Cloudflare dashboard.
2. Open the **Routes** tab.
3. Select **Add route**.
4. Select **Published application**.
5. Enter the required public hostname.
6. Set the service type to `HTTP`.
7. Set the service URL to `http://localhost:8080`.
8. Save the route.
9. Open the public hostname.
10. Open `/actuator/health/liveness` on the public hostname.

Cloudflare terminates public HTTPS and sends HTTP traffic through the outbound tunnel to the local application.

## Restrict access

The published hostname is public unless you add a Cloudflare Access policy. Add an Access application when the dashboard must be private. Test the policy in a private browser window before you share the hostname.

Do not publish PostgreSQL. Publish only the application port.

## Operate the tunnel

Check the Linux service:

```bash
sudo systemctl status cloudflared
```

Restart the service after a configuration change:

```bash
sudo systemctl restart cloudflared
```

If the service cannot find its configuration file, install the service with an explicit configuration path. Cloudflare documents this condition for installations that use `sudo` because `sudo` can change the home directory.

## Verify the deployment

1. Run `docker compose ps`.
2. Check the local liveness endpoint.
3. Check the local readiness endpoint.
4. Check `sudo systemctl status cloudflared`.
5. Open the public dashboard.
6. Confirm that the dashboard data age changes after a collection.

Use the current Cloudflare instructions when the dashboard labels differ from this procedure: <https://developers.cloudflare.com/cloudflare-one/networks/connectors/cloudflare-tunnel/get-started/create-remote-tunnel/>.
