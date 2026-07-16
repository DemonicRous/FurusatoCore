# Furusato Core

Общее ядро линейки модов Furusato для Minecraft Forge 1.12.2.

Название Furusato (故郷) означает не просто место рождения, а родной край,
к которому тянется сердце: память о доме, традициях и деревенской жизни.

## Требования

- JDK 8
- Minecraft 1.12.2
- Minecraft Forge 14.23.5.2847 или новее для 1.12.2

## Сборка

```powershell
.\gradlew.bat build
```

Готовый JAR создаётся в `build/libs`.

Для подготовки среды разработки используйте `setupDecompWorkspace`, после чего
импортируйте Gradle-проект в IDE. Основной mod id — `furusatocore`.
