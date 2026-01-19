# Research Documentation

This folder contains comprehensive research documentation for the Dart-ACDC-generator Generator project.

## Documents Overview

### 1. OpenAPI Generator

#### [openapi-generator.md](./openapi-generator.md)
Basic overview of OpenAPI Generator - what it is, what it does, and its key features.

**Topics covered**:
- What is OpenAPI Generator
- Main uses (client generation, server stubs, documentation)
- Key features and capabilities
- Integration methods

**Source**: https://openapi-generator.tech/

---

#### [creating-generators.md](./creating-generators.md)
Comprehensive guide on how to create custom OpenAPI generators.

**Topics covered**:
- How OpenAPI Generator works (architecture)
- dart-dio generator detailed analysis
- Two methods to create generators (`new.sh` vs `meta`)
- Required files and project structure
- Mustache templating system
- Customization approaches
- Compilation and testing procedures

**Key sections**:
- Required Files (Codegen class, templates, SPI registration)
- Mustache Templating System (loops, conditionals, variables, lambdas)
- Template Resolution Priority
- Customization Approaches (user-defined templates, extracting templates, full custom generator)
- Project Structure
- Compilation and Testing

**Sources**:
- https://openapi-generator.tech/docs/generators/dart-dio
- https://openapi-generator.tech/docs/new-generator/
- https://openapi-generator.tech/docs/customization/
- https://openapi-generator.tech/docs/templating/

---

#### [dart-generator-quick-reference.md](./dart-generator-quick-reference.md)
Practical quick-start guide for creating Dart client generators.

**Topics covered**:
- Step-by-step bootstrap instructions
- Complete Java class example for Dart generator
- Ready-to-use Mustache templates
- Dart type mappings
- Common Mustache variables reference
- Testing strategies
- Common customizations

**Key sections**:
- Quick Start (6 steps from bootstrap to testing)
- Complete code examples for:
  - Codegen Java class
  - README.mustache template
  - pubspec.mustache template
  - api.mustache template
  - model.mustache template
- Mustache variables reference (API, Operation, Parameter, Model levels)
- Common Dart type mappings
- Testing approaches
- Common customizations (generator options, method overrides)

**This is your go-to guide when implementing the generator.**

---

### 2. Dart-ACDC Library

#### [dart-acdc-library.md](./dart-acdc-library.md)
Complete documentation of the Dart-ACDC library - the foundation for integration.

**Topics covered**:
- What is Dart-ACDC (the 4 pillars: Authentication, Caching, Debugging, Client)
- Installation and quick start
- Authentication (OAuth 2.1, token refresh, storage)
- Caching (two-tier, user isolation, SWR)
- Logging and error handling
- Request deduplication
- Offline support
- Certificate pinning
- Integration with OpenAPI Generator
- Architecture and design patterns
- Dependencies
- Security features

**Key sections**:
- The Four Pillars (ACDC) with detailed explanations
- Configuration examples for each feature
- Code examples for common use cases
- Exception hierarchy and error handling
- Security architecture
- Best practices

**This is your reference for understanding what features to integrate.**

**Source**: https://github.com/jhosm/Dart-ACDC

**Documentation sources**:
- https://github.com/jhosm/Dart-ACDC/tree/main/doc
  - getting_started.md
  - authentication.md
  - caching.md
  - logging_and_errors.md
  - deduplication.md
  - offline.md
  - certificate_pinning.md

---

### 3. Project Vision

#### [project-vision.md](./project-vision.md)
The strategic vision for the Dart-ACDC-generator Generator project.

**Topics covered**:
- The problem we're solving
- The opportunity
- What is FAST (Fully Automated Setup and Templating)
- Goals and features
- Generated code examples
- Generator options and configuration
- Template structure
- Integration workflow
- Benefits for developers and teams
- Technical approach (5 phases)
- Challenges and solutions
- Success metrics

**Key sections**:
- The Problem (gap between generated clients and production apps)
- The Opportunity (automatic integration)
- The Dart-ACDC-generator Generator Vision
- Generated Code Example
- Key Features to Generate
- Generator Options (configuration)
- Template Structure
- Mustache Variable Mapping
- Type Mappings
- Integration Workflow (5 steps for developers)
- Benefits (for developers and teams)
- Technical Approach (5 phases of development)
- Challenges & Solutions
- Success Metrics
- Next Steps

**This is your roadmap for building the generator.**

---

## Quick Navigation

### I want to...

**...understand what OpenAPI Generator is**
→ Start with [openapi-generator.md](./openapi-generator.md)

**...learn how to create a custom generator**
→ Read [creating-generators.md](./creating-generators.md) first, then [dart-generator-quick-reference.md](./dart-generator-quick-reference.md)

**...start implementing the generator NOW**
→ Go to [dart-generator-quick-reference.md](./dart-generator-quick-reference.md)

**...understand Dart-ACDC features**
→ Read [dart-acdc-library.md](./dart-acdc-library.md)

**...understand the project goals and vision**
→ Read [project-vision.md](./project-vision.md)

**...see the development roadmap**
→ Check the "Technical Approach" section in [project-vision.md](./project-vision.md)

---

## Summary

### The Big Picture

1. **OpenAPI Generator** creates API clients from OpenAPI specs
2. **Dart-ACDC** provides production-ready HTTP client features (auth, caching, etc.)
3. **Gap**: Manual integration is time-consuming and error-prone
4. **Solution**: Create a custom generator that automatically integrates Dart-ACDC

### What We're Building

**Dart-ACDC-generator Generator** = Custom OpenAPI Generator that produces:
- Dart API clients
- Fully integrated with Dart-ACDC
- Production-ready code
- Type-safe error handling
- Authentication built-in
- Caching and offline support
- Comprehensive documentation

### Key Technologies

- **Language**: Java (for generator), Dart (for generated code)
- **Templating**: Mustache
- **HTTP Client**: Dio (via Dart-ACDC)
- **Target Platform**: Flutter (mobile, desktop, web)
- **Base**: Extends/inspired by dart-dio generator

### Development Phases

1. ✅ **Study & Research** (Current)
2. **Generator Development** (Bootstrap, implement, test)
3. **Refinement** (Options, templates, tests)
4. **Documentation** (Usage, developer, migration guides)
5. **Distribution** (Contribute, package, publish)

---

## Resources

### Official Documentation

- **OpenAPI Generator**: https://openapi-generator.tech/
- **Dart-ACDC**: https://github.com/jhosm/Dart-ACDC
- **Dio**: https://pub.dev/packages/dio

### Generator Development

- **New Generator Guide**: https://openapi-generator.tech/docs/new-generator/
- **Customization Guide**: https://openapi-generator.tech/docs/customization/
- **Templating Guide**: https://openapi-generator.tech/docs/templating/
- **Contributing Guide**: https://openapi-generator.tech/docs/contributing/

### Example Generators

- **Dart-Dio Generator**: https://openapi-generator.tech/docs/generators/dart-dio
- **Source Code**: `modules/openapi-generator/src/main/java/org/openapitools/codegen/languages/DartDioClientCodegen.java`

### Repository

- **OpenAPI Generator GitHub**: https://github.com/OpenAPITools/openapi-generator
- **Dart-ACDC GitHub**: https://github.com/jhosm/Dart-ACDC

---

## Notes for Development

### Prerequisites

1. Java Development Kit (JDK) 11+
2. Maven 3.8+
3. Git
4. Dart SDK 3.0+ (for testing generated code)
5. Flutter 3.10+ (for testing generated code)

### Development Workflow

1. Clone OpenAPI Generator repo
2. Run `./new.sh -n dart-acdC-generator -c -t`
3. Implement Codegen class
4. Create Mustache templates
5. Build: `mvn clean package -DskipTests`
6. Test: `./bin/generate-samples.sh ./bin/configs/dart-acdC-generator-petstore.yaml`
7. Verify: `cd samples/client/petstore/dart-acdC-generator && dart analyze`

### Key Files to Create

- `modules/openapi-generator/src/main/java/org/openapitools/codegen/languages/DartAcdcFastGenerator.java`
- `modules/openapi-generator/src/main/resources/META-INF/services/org.openapitools.codegen.CodegenConfig`
- `modules/openapi-generator/src/main/resources/dart-acdC-generator/*.mustache`
- `bin/configs/dart-acdC-generator-petstore.yaml`

### Testing Strategy

1. **Unit tests**: Test Codegen class methods
2. **Integration tests**: Generate from Petstore spec
3. **Manual tests**: Test generated client in real Flutter app
4. **Regression tests**: Ensure regeneration produces consistent results

---

## Changelog

### 2026-01-18
- ✅ Researched OpenAPI Generator
- ✅ Studied dart-dio generator
- ✅ Researched Dart-ACDC library (all 7 documentation files)
- ✅ Created comprehensive documentation
- ✅ Defined project vision and roadmap

### Next Steps
- [ ] Set up OpenAPI Generator development environment
- [ ] Bootstrap the generator using `new.sh`
- [ ] Implement basic Codegen class
- [ ] Create initial Mustache templates
- [ ] Test with Petstore spec

---

**Ready to build? Start with [dart-generator-quick-reference.md](./dart-generator-quick-reference.md)!**
