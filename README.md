# PDG Plagiarism Detector

A Java-based plagiarism detection tool that uses Program Dependence Graphs (PDG) to identify structural similarities between code submissions. This tool analyzes Java source code by constructing control flow graphs (CFG) and data dependence graphs (DDG) to create comprehensive program dependence graphs for comparison.

## How It Works

The plagiarism detector employs a sophisticated multi-step approach:

1. **Code Parsing**: Uses JavaParser to analyze Java source files and extract method declarations
2. **Graph Construction**:
   - Builds Control Flow Graphs (CFG) to represent program flow
   - Constructs Data Dependence Graphs (DDG) to capture variable dependencies
   - Combines both into Program Dependence Graphs (PDG)
3. **Similarity Analysis**: Uses γ-isomorphism algorithm to compare PDGs between submissions
4. **Scoring**: Calculates similarity scores based on graph structure matching

## Installation

1. Clone the repository:

```bash
git clone <repository-url>
cd PDG-Plagiarism-Detector
```

2. Build the project:

```bash
mvn clean package
```

3. The executable JAR will be created in the `target/` directory.

## Usage

### Basic Usage

```bash
java -jar target/plagurism_graph_comparison-0.1.0.jar <path-to-submissions-directory>
```

### Options

- `-d, --debugging`: Enable debug mode to output underlying graph structures
- `<path>`: Path to directory containing submission folders (each subfolder represents one submission)


## Configuration

The tool provides several configurable parameters to adjust detection sensitivity:

### Sensitivity Parameters

Located in `PlagiarismDetector.java`:

- **gamma** (default: 0.8): Controls the threshold for γ-isomorphism matching
- **minimum_node_count** (default: 10): Minimum number of nodes required in a graph for analysis
- **remove_insignificant_edges** (default: true): Whether to remove edges that don't contribute to structural analysis

## Output

The tool outputs similarity scores for each pair of submissions:

```
Directory Parsed, found 5 submissions and 23 methods
Score for submission pair student1 and student2 is 0.85
Score for submission pair student1 and student3 is 0.23
Score for submission pair student2 and student3 is 0.91
...
```

Higher scores indicate greater similarity and potential plagiarism.