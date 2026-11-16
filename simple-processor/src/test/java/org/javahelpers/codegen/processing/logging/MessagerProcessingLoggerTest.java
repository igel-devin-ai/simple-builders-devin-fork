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

package org.javahelpers.codegen.processing.logging;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.javahelpers.codegen.testing.ProbeProcessor;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MessagerProcessingLogger} driven by a real Messager. */
class MessagerProcessingLoggerTest {

  private static final String DTO_SOURCE =
      """
      package test;
      public class Dto {
        public void setName(String name) {}
      }
      """;

  /** Recorded diagnostic: kind, message, attached element. */
  private record Diag(Diagnostic.Kind kind, String message, Element element) {}

  @Test
  void warning_attachesElementInDefaultMode() {
    List<Diag> diags = new CopyOnWriteArrayList<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      MessagerProcessingLogger logger =
                          MessagerProcessingLogger.create(recordingEnv(env, diags));
                      Element param =
                          method(typeElement(env, "test.Dto"), "setName").getParameters().get(0);
                      logger.warning(param, "conflict on %s", "name");
                      logger.flushWarningLocations();
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertEquals(1, diags.size());
    Diag diag = diags.get(0);
    assertEquals(Diagnostic.Kind.WARNING, diag.kind());
    assertTrue(diag.message().contains("conflict on name"));
    assertEquals("name", diag.element().getSimpleName().toString());
  }

  @Test
  void error_attachesElement() {
    List<Diag> diags = new CopyOnWriteArrayList<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      MessagerProcessingLogger logger =
                          MessagerProcessingLogger.create(recordingEnv(env, diags));
                      logger.error(typeElement(env, "test.Dto"), "failed on %s", "Dto");
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    // the emitted error fails the compilation on purpose
    assertThat(compilation).failed();
    assertEquals(1, diags.size());
    assertEquals(Diagnostic.Kind.ERROR, diags.get(0).kind());
    assertEquals("Dto", diags.get(0).element().getSimpleName().toString());
  }

  @Test
  void verboseWarning_deferLocationAndFlushOncePerType() {
    List<Diag> diags = new CopyOnWriteArrayList<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      MessagerProcessingLogger logger =
                          MessagerProcessingLogger.createVerbose(recordingEnv(env, diags), "test");
                      Element param =
                          method(typeElement(env, "test.Dto"), "setName").getParameters().get(0);
                      logger.warning(param, "first %s", "a");
                      logger.warning(param, "second %s", "b");
                      logger.flushWarningLocations();
                      logger.flushWarningLocations(); // cleared -> nothing more
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertEquals(3, diags.size());
    // two flat warnings without element + one location marker on the enclosing type
    assertTrue(diags.get(0).message().contains("first a"));
    assertNull(diags.get(0).element());
    assertTrue(diags.get(2).message().contains("Location of the warning(s)"));
    assertEquals("Dto", diags.get(2).element().getSimpleName().toString());
  }

  @Test
  void deferWarningLocation_ignoresNullElement() {
    List<Diag> diags = new CopyOnWriteArrayList<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      MessagerProcessingLogger logger =
                          MessagerProcessingLogger.createVerbose(recordingEnv(env, diags));
                      logger.warning((Element) null, "orphan warning");
                      logger.flushWarningLocations();
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertEquals(1, diags.size());
    assertTrue(diags.get(0).message().contains("orphan warning"));
  }

  @Test
  void loggingPrefix_appliesToMessages() {
    List<Diag> diags = new CopyOnWriteArrayList<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) ->
                        MessagerProcessingLogger.create(recordingEnv(env, diags), "myproc")
                            .info("hello %s", "world")))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertTrue(diags.get(0).message().contains("myproc"));
  }

  /** Returns an environment identical to {@code env} but with a diagnostic-recording Messager. */
  private static ProcessingEnvironment recordingEnv(ProcessingEnvironment env, List<Diag> diags) {
    Messager delegate = env.getMessager();
    Messager recorder =
        new Messager() {
          @Override
          public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            diags.add(new Diag(kind, msg.toString(), null));
            delegate.printMessage(kind, msg);
          }

          @Override
          public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            diags.add(new Diag(kind, msg.toString(), e));
            delegate.printMessage(kind, msg, e);
          }

          @Override
          public void printMessage(
              Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
            delegate.printMessage(kind, msg, e, a);
          }

          @Override
          public void printMessage(
              Diagnostic.Kind kind,
              CharSequence msg,
              Element e,
              AnnotationMirror a,
              AnnotationValue v) {
            delegate.printMessage(kind, msg, e, a, v);
          }
        };
    return new ProcessingEnvironment() {
      @Override
      public Map<String, String> getOptions() {
        return env.getOptions();
      }

      @Override
      public Messager getMessager() {
        return recorder;
      }

      @Override
      public Filer getFiler() {
        return env.getFiler();
      }

      @Override
      public Elements getElementUtils() {
        return env.getElementUtils();
      }

      @Override
      public Types getTypeUtils() {
        return env.getTypeUtils();
      }

      @Override
      public SourceVersion getSourceVersion() {
        return env.getSourceVersion();
      }

      @Override
      public Locale getLocale() {
        return env.getLocale();
      }
    };
  }
}
