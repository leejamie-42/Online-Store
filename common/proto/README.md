# Common Proto Definitions

This directory contains shared gRPC proto definitions used across multiple microservices.

## Purpose

Proto files here serve as the **single source of truth** for service contracts between:
- **Store Backend** (gRPC client)
- **Warehouse Service** (gRPC server)
- Future services requiring these interfaces

## Structure

```
common/
└── proto/
    └── warehouse.proto    # Warehouse inventory operations contract
```

## Benefits

1. **Version Consistency**: Both client and server use identical contract versions
2. **No Duplication**: Single proto definition eliminates sync issues
3. **Easy Maintenance**: One place to update interface changes
4. **Standard Pattern**: Follows microservices best practices

## Usage

### Store Backend (gRPC Client)

The store-backend references this directory in `build.gradle`:

```gradle
sourceSets {
    main {
        proto {
            srcDir '../common/proto'  // Reference shared protos
        }
    }
}
```

Generate Java classes:
```bash
./gradlew :store-backend:generateProto
```

### Warehouse Service (gRPC Server)

When implementing the warehouse service, configure similarly:

```gradle
sourceSets {
    main {
        proto {
            srcDir '../common/proto'  // Same shared protos
        }
    }
}
```

## Proto Files

### warehouse.proto

Defines the WarehouseService with operations:

| Operation      | Purpose                              | Used By       |
|----------------|--------------------------------------|---------------|
| CheckStock     | Verify product availability          | Store Backend |
| ReserveStock   | Atomically reserve inventory         | Store Backend |
| CommitStock    | Mark reserved stock as sold          | Store Backend |
| RollbackStock  | Release reserved stock (rollback)    | Store Backend |

**Package**: `warehouse`
**Java Package**: `com.comp5348.store.grpc.warehouse`

## Versioning

When making **breaking changes** to proto files:

1. Consider creating versioned proto files (e.g., `warehouse_v2.proto`)
2. Maintain backward compatibility when possible
3. Coordinate updates across all services using the contract
4. Test both client and server after proto changes

## Adding New Proto Files

1. Place `.proto` file in `common/proto/`
2. Update relevant service `build.gradle` to reference it
3. Generate proto classes: `./gradlew generateProto`
4. Implement service logic in respective microservices

## Best Practices

- ✅ Use `snake_case` for field names (proto convention)
- ✅ Use `PascalCase` for message and service names
- ✅ Document all services, RPCs, and messages
- ✅ Include comments explaining business logic
- ✅ Use appropriate data types (int64 for IDs, int32 for quantities)
- ✅ Test proto changes in both client and server
- ❌ Don't duplicate proto files in individual services
- ❌ Don't make breaking changes without coordination

## References

- [gRPC Style Guide](https://protobuf.dev/programming-guides/style/)
- [gRPC Java Documentation](https://grpc.io/docs/languages/java/)
- Project docs: `docs/SYSTEM_INTERFACE_SPEC.md`
