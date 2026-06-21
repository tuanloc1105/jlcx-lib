# Agent Workflows

Use the narrowest meaningful verification. Prefer module-level checks before full builds.

## Maven

Root Maven modules are `common-lib` and `processor`.

```bash
mvn -pl common-lib test
mvn -pl processor test
mvn clean install
```

Focused tests:

```bash
mvn -pl common-lib -Dtest=ClassNameTest test
```

Processor behavior is tested from `common-lib/src/test/java/vn/io/lcx/processor/`; useful focused tests include `ControllerProcessorTest`, `RestControllerProcessorTest`, `MapperClassProcessorTest`, `CodeTemplatesTest`, `RepositoryProcessorTest`, `ReactiveRepositoryProcessorTest`, and `HRRepositoryProcessorTest`.

If generated-code behavior changes, verify both processor tests and at least one downstream example compile.

## Root Scripts

Unix/Linux:

- `build.sh`
- `clean.sh`
- `snapshot.sh`
- `release.sh`

macOS JDK layout:

- `osx-build.sh`

Windows PowerShell:

- `build.ps1`
- `clean.ps1`
- `deploy.ps1`
- `snapshot.ps1`
- `release.ps1`

The build scripts set local JDK/Maven paths and may assume developer machine paths. Inspect before relying on them in automation.

## Frontend Checks

Todo UI:

```bash
cd examples/todo-app-example/web
pnpm install
pnpm run lint
pnpm run build
```

Hibernate Reactive UI:

```bash
cd examples/hibernate-reactive-example/web
pnpm install
pnpm run lint
pnpm run build
```

Both example frontends have `pnpm-lock.yaml`. No Vitest/Jest/Playwright config is checked in.

## Example Backends

Todo app:

```bash
cd examples/todo-app-example
mvn clean package
./build.sh
./clean.sh
```

PowerShell scripts also exist: `examples/todo-app-example/build.ps1`, `examples/todo-app-example/clean.ps1`, `examples/todo-app-example/run.ps1`.

Hibernate Reactive example:

```bash
cd examples/hibernate-reactive-example
mvn clean package
./build.sh
./start.sh
./stop.sh
./clean.sh
```

PowerShell scripts also exist: `examples/hibernate-reactive-example/build.ps1`, `examples/hibernate-reactive-example/clean.ps1`.

## gRPC Example

Read `examples/grpc-example/README.md` and `docs/examples.md` first.

Contract source:

- `examples/grpc-example/proto/hello.proto`

Generation/build scripts:

- `examples/grpc-example/build.sh`
- `examples/grpc-example/build.ps1`
- `examples/grpc-example/clean.sh`
- `examples/grpc-example/clean.ps1`

Generated stubs may appear under `target/generated-sources/` or generated `com/example/grpc/` packages after the build. Edit the proto first and regenerate rather than hand-editing generated stubs.

Port note: current Java source listens/connects on `7070`; `examples/grpc-example/README.md` still mentions `9090`.

## CI And Quality

No checked-in CI workflow files were found. There is no `.github/workflows/` directory in this checkout.

Use local checks that match the changed surface:

- Library behavior: `mvn -pl common-lib test`
- Processor facade: `mvn -pl processor test`
- Cross-module compiler/codegen behavior: `mvn clean install`
- Todo frontend: `pnpm run lint && pnpm run build` in `examples/todo-app-example/web`
- Hibernate Reactive frontend: `pnpm run lint && pnpm run build` in `examples/hibernate-reactive-example/web`

## Release And Deploy Scripts

Before touching release/deploy behavior, inspect:

- `pom.xml`
- `build.sh`, `build.ps1`
- `snapshot.sh`, `snapshot.ps1`
- `release.sh`, `release.ps1`
- `deploy.ps1`

Do not infer production deployment behavior from examples without checking `docs/agent/operations.md`.
