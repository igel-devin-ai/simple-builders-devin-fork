/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.javahelpers.simple.builders.processor.testing;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import java.util.List;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.apache.commons.lang3.Strings;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.javahelpers.simple.builders.processor.BuilderProcessor;

/**
 * Utilities to simplify simple-builders processor tests: builder-annotated source fixtures and a
 * compiler preconfigured with {@link BuilderProcessor} and the domain's verbose option. The
 * underlying compile-testing machinery lives in {@link CodegenTestUtils}.
 */
public final class ProcessorTestUtils {

  private ProcessorTestUtils() {}

  /**
   * Creates a configured {@link Compiler} instance with the BuilderProcessor.
   *
   * <p>This method checks for the system property {@code simplebuilder.verbose} (or {@code
   * Averbose}) and automatically adds {@code -Averbose=true} to the compiler options if either is
   * set to "true".
   *
   * <p>This allows developers to enable verbose processor output for all tests by running: {@code
   * mvn test -Dsimplebuilder.verbose=true}
   *
   * @return a Compiler instance configured with BuilderProcessor and optional verbose output
   */
  public static Compiler createCompiler() {
    return createCompiler(new BuilderProcessor());
  }

  /**
   * Compiles the given sources with the {@link BuilderProcessor} and prints the diagnostics when
   * verbose mode is enabled.
   *
   * @param sources the sources to compile
   * @return the compilation result
   */
  public static Compilation compile(JavaFileObject... sources) {
    Compilation compilation = createCompiler().compile(sources);
    printDiagnosticsOnVerbose(compilation);
    return compilation;
  }

  /**
   * Creates a configured {@link Compiler} instance with the given processors in invocation order.
   *
   * <p>Use this overload when a test compiles with several annotation processors whose relative
   * order matters (e.g. {@code BuilderProcessor} alongside the MapStruct {@code MappingProcessor}).
   * Verbose handling matches {@link #createCompiler()}.
   *
   * @param processors the processors to register, in the order javac invokes them
   * @return a Compiler instance configured with the given processors and optional verbose output
   */
  public static Compiler createCompiler(Processor... processors) {
    Compiler compiler = CodegenTestUtils.createCompiler(processors);

    // Check for verbose flag from Maven property
    if (isVerboseEnabled()) {
      compiler = compiler.withOptions("-Averbose=true");
    }

    return compiler;
  }

  /**
   * Asserts that none of the compilation's warnings contains the given text (case-insensitive).
   *
   * @param compilation the compilation result to check
   * @param text the text no warning message may contain
   */
  public static void assertNoWarningContaining(Compilation compilation, String text) {
    CodegenTestUtils.assertNoWarningContaining(compilation, text);
  }

  /**
   * Checks if verbose mode is enabled via system properties.
   *
   * @return true if simplebuilder.verbose or Averbose is set to "true"
   */
  public static boolean isVerboseEnabled() {
    String verboseProperty = System.getProperty("simplebuilder.verbose");
    String averboseProperty = System.getProperty("Averbose");
    return Strings.CI.equalsAny("true", verboseProperty, averboseProperty);
  }

  /**
   * Prints compilation diagnostics (notes, warnings, errors) and generated source files to
   * System.out if verbose mode is enabled.
   *
   * <p>This is useful for debugging test failures, as it makes the processor's debug output and
   * generated code visible in the test console output and CI logs.
   *
   * @param compilation the compilation result to print diagnostics from
   */
  public static void printDiagnosticsOnVerbose(Compilation compilation) {
    if (isVerboseEnabled()) {
      CodegenTestUtils.printDiagnostics(compilation);
    }
  }

  /**
   * Prints all generated source files to System.out if verbose mode is enabled.
   *
   * <p>This displays the actual generated code before assertions run, making it easy to compare
   * expected vs actual output without debugging.
   *
   * @param compilation the compilation result containing generated files
   */
  public static void printGeneratedSourcesOnVerbose(Compilation compilation) {
    if (isVerboseEnabled()) {
      CodegenTestUtils.printGeneratedSources(compilation);
    }
  }

  /**
   * Creates a {@link JavaFileObject} for a simple class annotated with @SimpleBuilder. You pass the
   * inner body lines (fields/methods); imports and annotation are handled for you.
   *
   * @return JavaFileObject suitable for compilation testing
   */
  public static JavaFileObject simpleBuilderClass(
      String packageName, String className, List<String> bodyLines) {
    String[] lines = buildSourceLines(packageName, className, bodyLines);
    String source = String.join("\n", lines);
    String fqcn = packageName + "." + className;
    return com.google.testing.compile.JavaFileObjects.forSourceString(fqcn, source);
  }

  /**
   * Varargs overload for convenience.
   *
   * @return JavaFileObject suitable for compilation testing
   */
  public static JavaFileObject simpleBuilderClass(
      String packageName, String className, String... bodyLines) {
    return simpleBuilderClass(packageName, className, List.of(bodyLines));
  }

  /**
   * Creates a {@link JavaFileObject} from source text, deriving the fully qualified class name from
   * the source's package and top-level type declaration.
   *
   * @param source the Java source code
   * @return JavaFileObject suitable for compilation testing
   * @throws IllegalArgumentException if the top-level type name cannot be determined
   */
  public static JavaFileObject forSource(String source) {
    return CodegenTestUtils.forSource(source);
  }

  /**
   * Load the generated builder source code by simple name from the given compilation. No assertions
   * are performed here; callers should assert separately.
   *
   * @param compilation the compilation result containing generated files
   * @param builderSimpleName the simple name of the generated builder
   * @return the generated source text
   */
  public static String loadGeneratedSource(Compilation compilation, String builderSimpleName) {
    return CodegenTestUtils.loadGeneratedSource(compilation, builderSimpleName);
  }

  private static String[] buildSourceLines(
      String packageName, String className, List<String> bodyLines) {
    // Build lines with imports + annotation + class skeleton
    String[] header =
        new String[] {
          "package " + packageName + ";",
          "",
          "import "
              + org.javahelpers.simple.builders.core.annotations.SimpleBuilder.class.getName()
              + ";",
          "",
          "@SimpleBuilder",
          "public class " + className + " {"
        };

    String[] footer = new String[] {"}"};

    String[] body = bodyLines.toArray(String[]::new);

    String[] lines = new String[header.length + body.length + footer.length];
    System.arraycopy(header, 0, lines, 0, header.length);
    System.arraycopy(body, 0, lines, header.length, body.length);
    System.arraycopy(footer, 0, lines, header.length + body.length, footer.length);
    return lines;
  }

  /**
   * Creates a mock annotation for testing without any members.
   *
   * @param packageName the package name of the annotation (e.g., "jakarta.validation")
   * @param annotationName the simple name of the annotation (e.g., "Valid")
   * @param targets the annotation targets (e.g., "ElementType.FIELD, ElementType.PARAMETER")
   * @return a JavaFileObject representing the mocked annotation
   */
  public static JavaFileObject createMockAnnotation(
      String packageName, String annotationName, String targets) {
    return CodegenTestUtils.createMockAnnotation(packageName, annotationName, targets);
  }

  /**
   * Creates a mock annotation for testing with optional members.
   *
   * @param packageName the package name of the annotation (e.g., "jakarta.validation")
   * @param annotationName the simple name of the annotation (e.g., "Valid")
   * @param targets the annotation targets (e.g., "ElementType.FIELD, ElementType.PARAMETER")
   * @param members the annotation members/attributes (e.g., "int min() default 0;")
   * @return a JavaFileObject representing the mocked annotation
   */
  public static JavaFileObject createMockAnnotation(
      String packageName, String annotationName, String targets, String members) {
    return CodegenTestUtils.createMockAnnotation(packageName, annotationName, targets, members);
  }
}
