# PWRUP Command Robot Base

## Introduction

Welcome to the **PWRUP Command Robot Base**! This repository serves as the foundation for building command-based robots that our team will create in the years ahead. By providing a structured framework, essential libraries, and key features, this template aims to streamline the development process and set our robots up for success.

### Key Benefits:
- **Modularity:** Easily fork this repository to kickstart a new robot project.
- **Reusability:** Built-in features and libraries ensure consistency and speed up development.
- **Scalability:** Designed to be expanded with additional functionality as our team's needs grow.

---

## Functionality

This template provides the core structure for building robots using the **Command-Based Programming** model. It includes essential features and tools to simplify the creation and deployment of robot software, ensuring that our team has a solid base to work from.

### Use Case:
When a new robot project is started, the team can **fork** this repository, allowing them to focus on adding custom code specific to their robot, while benefiting from the standardized tools and libraries already included.

---

## Features Overview

This repository includes several pre-configured features and libraries to accelerate development and promote best practices. As the team’s requirements evolve, we’ll continue to integrate more functionalities.

### Current Features:

1. **[Source vs. Online Library Building](./docs/SourceBuildingPlugin.md)**:
   - Enables developers to decide whether to build the robot software from the **latest source code** or from a pre-built **online library**.
   - This flexibility is particularly useful when working under tight deadlines or experimenting with local changes without needing to push to GitHub.
   - See the detailed documentation [here](./docs/SourceBuildingPlugin.md).

### Future Expansion:
- Additional libraries and features will be added as the team's needs grow, ensuring the template evolves with the requirements of our robot projects.

---

## Getting Started

### 1. Forking the Repository
To create a new robot, simply **fork this repository** and start adding your custom robot code. The template is designed to be modular, allowing you to build upon it with minimal configuration.

### 2. Using the Build System
By default, the repository is configured to use Gradle, with a built-in option for selecting **source vs. online builds**. This ensures that whether you're pulling the latest version of a library from a repository or building locally, the process is seamless.

Refer to the [SourceBuildingPlugin.md](./docs/SourceBuildingPlugin.md) for more information on configuring the build system based on your current needs.

### 3. Customizing Your Robot Code
Once you've forked the repository:
- Add your custom subsystems, commands, and robot logic.
- Leverage the pre-configured structure to ensure your robot code adheres to best practices and command-based programming principles.

---

## Contribution Guidelines

If you have ideas for new features or improvements to this template, please feel free to:
1. Fork the repository.
2. Create a branch for your feature or fix.
3. Submit a pull request with detailed information about the changes.

We encourage all team members to contribute to this repository so that it continues to meet the evolving needs of our robot projects.
