# unveiler

👺 Unveils Discord attachments when it gets deleted


`docker-compose.yml`

```yaml
version: '3.8'

services:
  db:
    image: mongo:5.0
    ports:
      - '27017:27017/tcp'
    volumes:
      - db:/data/db
    environment:
      TZ: Asia/Tokyo
      MONGO_INITDB_ROOT_USERNAME: ${MONGO_USERNAME}
      MONGO_INITDB_ROOT_PASSWORD: ${MONGO_PASSWORD}
  
  app:
    image: ghcr.io/slashnephy/unveiler:master
    volumes:
      - data:/app/data
    environment:
      # Discord Bot token
      DISCORD_TOKEN: xxx
      # message destination
      DISCORD_CHANNEL_ID: 100001
      MONGO_DATABASE_URI: mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@db:27017

volumes:
  db:
  data:
```
