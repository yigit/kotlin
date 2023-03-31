@file:Suppress("unused", "DuplicatedCode")

// DO NOT EDIT MANUALLY!
// Generated by generators/tests/org/jetbrains/kotlin/generators/arguments/GenerateCompilerArgumentsCopy.kt
// To regenerate run 'generateCompilerArgumentsCopy' task

package org.jetbrains.kotlin.cli.common.arguments

@OptIn(org.jetbrains.kotlin.utils.IDEAPluginsCompatibilityAPI::class)
fun copyK2JVMCompilerArguments(from: K2JVMCompilerArguments, to: K2JVMCompilerArguments): K2JVMCompilerArguments {
    copyCommonCompilerArguments(from, to)

    to.abiStability = from.abiStability
    to.additionalJavaModules = from.additionalJavaModules?.copyOf()
    to.allowNoSourceFiles = from.allowNoSourceFiles
    to.allowUnstableDependencies = from.allowUnstableDependencies
    to.assertionsMode = from.assertionsMode
    to.backendThreads = from.backendThreads
    to.buildFile = from.buildFile
    to.classpath = from.classpath?.copyOf()
    to.compileJava = from.compileJava
    to.declarationsOutputPath = from.declarationsOutputPath
    to.defaultScriptExtension = from.defaultScriptExtension
    to.destination = from.destination
    to.disableStandardScript = from.disableStandardScript
    to.doNotClearBindingContext = from.doNotClearBindingContext
    to.emitJvmTypeAnnotations = from.emitJvmTypeAnnotations
    to.enableDebugMode = from.enableDebugMode
    to.enableIrInliner = from.enableIrInliner
    to.enableJvmPreview = from.enableJvmPreview
    to.enhanceTypeParameterTypesToDefNotNull = from.enhanceTypeParameterTypesToDefNotNull
    to.expression = from.expression
    to.friendPaths = from.friendPaths?.copyOf()
    to.includeRuntime = from.includeRuntime
    to.inheritMultifileParts = from.inheritMultifileParts
    to.javaModulePath = from.javaModulePath
    to.javaPackagePrefix = from.javaPackagePrefix
    to.javaParameters = from.javaParameters
    to.javaSourceRoots = from.javaSourceRoots?.copyOf()
    to.javacArguments = from.javacArguments?.copyOf()
    to.jdkHome = from.jdkHome
    to.jdkRelease = from.jdkRelease
    to.jspecifyAnnotations = from.jspecifyAnnotations
    to.jsr305 = from.jsr305?.copyOf()
    to.jvmDefault = from.jvmDefault
    to.jvmTarget = from.jvmTarget
    to.klibLibraries = from.klibLibraries
    to.lambdas = from.lambdas
    to.linkViaSignatures = from.linkViaSignatures
    to.moduleName = from.moduleName
    to.noCallAssertions = from.noCallAssertions
    to.noJdk = from.noJdk
    to.noKotlinNothingValueException = from.noKotlinNothingValueException
    to.noNewJavaAnnotationTargets = from.noNewJavaAnnotationTargets
    to.noOptimize = from.noOptimize
    to.noOptimizedCallableReferences = from.noOptimizedCallableReferences
    to.noParamAssertions = from.noParamAssertions
    to.noReceiverAssertions = from.noReceiverAssertions
    to.noReflect = from.noReflect
    to.noResetJarTimestamps = from.noResetJarTimestamps
    to.noSourceDebugExtension = from.noSourceDebugExtension
    to.noStdlib = from.noStdlib
    to.noUnifiedNullChecks = from.noUnifiedNullChecks
    to.nullabilityAnnotations = from.nullabilityAnnotations?.copyOf()
    to.oldInnerClassesLogic = from.oldInnerClassesLogic
    to.profileCompilerCommand = from.profileCompilerCommand
    to.repeatCompileModules = from.repeatCompileModules
    to.samConversions = from.samConversions
    to.sanitizeParentheses = from.sanitizeParentheses
    to.scriptResolverEnvironment = from.scriptResolverEnvironment?.copyOf()
    to.scriptTemplates = from.scriptTemplates?.copyOf()
    to.serializeIr = from.serializeIr
    to.strictMetadataVersionSemantics = from.strictMetadataVersionSemantics
    to.stringConcat = from.stringConcat
    to.supportCompatqualCheckerFrameworkAnnotations = from.supportCompatqualCheckerFrameworkAnnotations
    to.suppressDeprecatedJvmTargetWarning = from.suppressDeprecatedJvmTargetWarning
    to.suppressMissingBuiltinsError = from.suppressMissingBuiltinsError
    to.typeEnhancementImprovementsInStrictMode = from.typeEnhancementImprovementsInStrictMode
    to.useFastJarFileSystem = from.useFastJarFileSystem
    to.useIR = from.useIR
    to.useJavac = from.useJavac
    to.useOldBackend = from.useOldBackend
    to.useOldClassFilesReading = from.useOldClassFilesReading
    to.useOldInlineClassesManglingScheme = from.useOldInlineClassesManglingScheme
    to.useTypeTable = from.useTypeTable
    to.validateBytecode = from.validateBytecode
    to.validateIr = from.validateIr
    to.valueClasses = from.valueClasses

    return to
}
