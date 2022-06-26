# unveiler

👺 Unveils Discord attachments when it gets deleted

[![Kotlin](https://img.shields.io/badge/Kotlin-1.7-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/unveiler)](https://github.com/SlashNephy/unveiler/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/unveiler/Docker)](https://hub.docker.com/r/slashnephy/unveiler)
[![license](https://img.shields.io/github/license/SlashNephy/unveiler)](https://github.com/SlashNephy/unveiler/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/unveiler)](https://github.com/SlashNephy/unveiler/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/unveiler)](https://github.com/SlashNephy/unveiler/pulls)

## Requirements

- Java 17 or later

## Get Started

### Docker

There are some image tags.

- `ghcr.io/slashnephy/unveiler:master`  
  Automatically published every push to `master` branch.
- `ghcr.io/slashnephy/unveiler:<version>`  
  Coresponding to release tags on GitHub.

`docker-compose.yml`

```yaml
version: '3.8'

services:
  unveiler:
    container_name: unveiler
    image: ghcr.io/slashnephy/unveiler:master
    restart: always
    volumes:
      - data:/app/data
    environment:
      # Discord Bot token
      DISCORD_TOKEN: xxx
      # message destination
      DISCORD_CHANNEL_ID: 100001

volumes:
  data:
    driver: local
```
