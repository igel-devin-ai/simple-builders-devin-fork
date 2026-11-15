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

package org.javahelpers.codegen.testing;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Assertions;

/**
 * Generic utilities for annotation-processor tests: building source files, compiling them with
 * Google Compile Testing, and inspecting the resulting compilation. Domain-specific conveniences
 * (concrete processors, domain option names, domain annotations) live in the domain's own test
 * utilities on top of these.
 */
public final class CodegenTestUtils {

  private static final Pattern PACKAGE_PATTERN =
      Pattern.compile("(?m)^\\s*package\\s+([a-zA-Z_]\\w*(?:\\.[a-zA-Z_]\\w*)*)\\s*;");

  private static final Pattern TOP_LEVEL_TYPE_PATTERN =
      Pattern.compile(
          "(?m)^[ \\t]*(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed|strictfp)\\s+)*(?:@?interface|class|enum|record)\\s+([A-Za-z_]\\w*)\\b");

  private CodegenTestUtils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Creates a {@link Compiler} instance with the given processors in invocation order.
   *
   * <p>Use this overload when a test compiles with several annotation processors whose relative
   * order matters (e.g. a domain processor alongside the MapStruct {@code MappingProcessor}).
   *
   * @param processors the processors to register, in the order javac invokes them
   * @return a Compiler instance configured with the given processors
   */
  public static Compiler createCompiler(Processor... processors) {
    return Compiler.javac().withProcessors(processors);
  }

  /**
   * Asserts that none of the compilation's warnings contains the given text (case-insensitive).
   *
   * @param compilation the compilation result to check
   * @param text the text no warning message may contain
   */
  public static void assertNoWarningContaining(Compilation compilation, String text) {
    String warnings =
        compilation.warnings().stream()
            .map(diagnostic -> diagnostic.getMessage(null))
            .collect(Collectors.joining("\n"));
    Assertions.assertFalse(
        warnings.toLowerCase().contains(text.toLowerCase()),
        "No warning containing '" + text + "' expected, got: " + warnings);
  }

  /**
   * Prints compilation diagnostics (notes, warnings, errors) and generated source files to
   * System.out.
   *
   * <p>This is useful for debugging test failures, as it makes the processor's debug output and
   * generated code visible in the test console output and CI logs. Domains decide when to call this
   * (e.g. gated on their verbose flag).
   *
   * @param compilation the compilation result to print diagnostics from
   */
  public static void printDiagnostics(Compilation compilation) {
    System.out.println("\n========== Compilation Diagnostics ==========");

    // Print notes (includes debug messages)
    if (!compilation.notes().isEmpty()) {
      System.out.println("--- NOTES ---");
      compilation.notes().forEach(diag -> System.out.println(diag.getMessage(null)));
    }

    // Print warnings
    if (!compilation.warnings().isEmpty()) {
      System.out.println("\n--- WARNINGS ---");
      compilation.warnings().forEach(diag -> System.out.println(diag.getMessage(null)));
    }

    // Print errors
    if (!compilation.errors().isEmpty()) {
      System.out.println("\n--- ERRORS ---");
      compilation.errors().forEach(diag -> System.out.println(diag.getMessage(null)));
    }

    System.out.println("=============================================\n");

    // Print generated source files
    printGeneratedSources(compilation);
  }

  /**
   * Prints all generated source files to System.out.
   *
   * <p>This displays the actual generated code before assertions run, making it easy to compare
   * expected vs actual output without debugging.
   *
   * @param compilation the compilation result containing generated files
   */
  public static void printGeneratedSources(Compilation compilation) {
    var generatedFiles = compilation.generatedSourceFiles();
    if (generatedFiles.isEmpty()) {
      System.out.println("========== No Source Files Generated ==========\n");
      return;
    }

    System.out.println("========== Generated Source Files ==========");
    generatedFiles.forEach(
        file -> {
          try {
            String fileName = file.getName();
            String content = file.getCharContent(false).toString();
            System.out.println("\n--- " + fileName + " ---");
            System.out.println(content);
            System.out.println("--- End of " + fileName + " ---");
          } catch (Exception e) {
            System.err.println("Failed to read generated file: " + e.getMessage());
          }
        });
    System.out.println("=============================================\n");
  }

  /**
   * Creates a {@link JavaFileObject} from source text. The fully qualified class name is derived
   * from the source's package and top-level type declaration.
   *
   * @param source the Java source code
   * @return JavaFileObject suitable for compilation testing
   * @throws IllegalArgumentException if the top-level type name cannot be determined
   */
  public static JavaFileObject forSource(String source) {
    String pkg = extractPackageName(source);
    String type = extractTopLevelTypeName(source);
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("Cannot determine top-level type name from source.");
    }
    String fqcn = (pkg == null || pkg.isBlank()) ? type : (pkg + "." + type);
    return JavaFileObjects.forSourceString(fqcn, source);
  }

  /**
   * Load a generated source file by simple class name from the given compilation. No assertions are
   * performed here; callers should assert separately.
   *
   * @param compilation the compilation result containing generated files
   * @param generatedSimpleName the simple name of the generated type (e.g. {@code
   *     PersonDtoBuilder})
   * @return the generated source text
   */
  public static String loadGeneratedSource(Compilation compilation, String generatedSimpleName) {
    JavaFileObject file =
        compilation.generatedFiles().stream()
            .filter(f -> f.getKind() == JavaFileObject.Kind.SOURCE)
            .filter(f -> f.getName().endsWith(generatedSimpleName + ".java"))
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError("Generated source file not found: " + generatedSimpleName));
    return readSource(file);
  }

  /**
   * Reads the content of a source file object, for example one returned by {@link
   * Compilation#generatedSourceFile(String)}.
   *
   * @param file the file object to read
   * @return the content of the file
   */
  public static String readSource(JavaFileObject file) {
    try {
      return file.getCharContent(true).toString();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read source: " + file.getName(), e);
    }
  }

  /**
   * Creates a mock annotation for testing without any members.
   *
   * <p>Mock annotations are used instead of real dependencies because:
   *
   * <ul>
   *   <li>Annotation processors work at compile-time and only see annotation metadata, not
   *       implementations
   *   <li>Avoids adding unnecessary runtime dependencies to the test classpath
   *   <li>Ensures test isolation from external library versions
   *   <li>Follows standard practice for annotation processor testing with Google Compile Testing
   * </ul>
   *
   * <p>The processor only cares about the qualified name, not the actual implementation.
   *
   * @param packageName the package name of the annotation (e.g., "jakarta.validation")
   * @param annotationName the simple name of the annotation (e.g., "Valid")
   * @param targets the annotation targets (e.g., "ElementType.FIELD, ElementType.PARAMETER")
   * @return a JavaFileObject representing the mocked annotation
   */
  public static JavaFileObject createMockAnnotation(
      String packageName, String annotationName, String targets) {
    return createMockAnnotation(packageName, annotationName, targets, "");
  }

  /**
   * Creates a mock annotation for testing with optional members.
   *
   * <p>Mock annotations are used instead of real dependencies because:
   *
   * <ul>
   *   <li>Annotation processors work at compile-time and only see annotation metadata, not
   *       implementations
   *   <li>Avoids adding unnecessary runtime dependencies to the test classpath
   *   <li>Ensures test isolation from external library versions
   *   <li>Follows standard practice for annotation processor testing with Google Compile Testing
   * </ul>
   *
   * <p>The processor only cares about the qualified name, not the actual implementation.
   *
   * @param packageName the package name of the annotation (e.g., "jakarta.validation")
   * @param annotationName the simple name of the annotation (e.g., "Valid")
   * @param targets the annotation targets (e.g., "ElementType.FIELD, ElementType.PARAMETER")
   * @param members the annotation members/attributes (e.g., "int min() default 0;")
   * @return a JavaFileObject representing the mocked annotation
   */
  public static JavaFileObject createMockAnnotation(
      String packageName, String annotationName, String targets, String members) {
    String qualifiedName = packageName + "." + annotationName;
    String membersSection = members.isEmpty() ? "" : "\n  " + members + "\n";

    return JavaFileObjects.forSourceString(
        qualifiedName,
        """
        package %s;
        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;

        @Retention(RetentionPolicy.RUNTIME)
        @Target({%s})
        public @interface %s {%s}
        """
            .formatted(packageName, targets, annotationName, membersSection));
  }

  private static String extractPackageName(String source) {
    Matcher m = PACKAGE_PATTERN.matcher(source);
    return m.find() ? m.group(1) : null;
  }

  private static String extractTopLevelTypeName(String source) {
    Matcher m = TOP_LEVEL_TYPE_PATTERN.matcher(source);
    return m.find() ? m.group(1) : null;
  }
}
