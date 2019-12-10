package edu.columbia.cs.psl.phosphor;

import edu.columbia.cs.psl.phosphor.instrumenter.TaintTrackingClassVisitor;
import edu.columbia.cs.psl.phosphor.runtime.StringUtils;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import org.apache.commons.cli.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;
import java.util.concurrent.*;
import java.util.zip.*;

public class Instrumenter {

    public static ClassLoader loader;
    public static boolean IS_KAFFE_INST = Boolean.parseBoolean(System.getProperty("KAFFE", "false"));
    public static boolean IS_HARMONY_INST = Boolean.parseBoolean(System.getProperty("HARMONY", "false"));
    public static Map<String, ClassNode> classes = new HashMap<>();
    public static InputStream sourcesFile;
    public static InputStream sinksFile;
    public static InputStream taintThroughFile;
    public static boolean ANALYZE_ONLY;
    static String curPath;
    static int nTotal = 0;
    static int n = 0;
    static Option opt_withoutDataTrack = Option.builder("withoutDataTrack")
            .desc("Disable taint tracking through data flow (on by default)")
            .build();
    static Option opt_controlTrack = Option.builder("controlTrack")
            .desc("Enable taint tracking through control flow")
            .build();
    static Option opt_controlLightTrack = Option.builder("lightControlTrack")
            .desc("Enable taint tracking through control flow, but does NOT propagate control dependencies between methods")
            .build();
    static Option opt_controlTrackExceptions = Option.builder("controlTrackExceptions")
            .desc("Enable taint tracking through exceptional control flow")
            .build();
    static Option opt_withoutBranchNotTaken = Option.builder("withoutBranchNotTaken")
            .desc("Disable branch not taken analysis in control tracking")
            .build();
    static Option opt_trackArrayLengthTaints = Option.builder("withArrayLengthTags")
            .desc("Tracks taint tags on array lengths - requires use of JVMTI runtime library when running")
            .build();
    static Option opt_trackArrayIndexTaints = Option.builder("withArrayIndexTags")
            .desc("Tracks taint tags from array indices to values get/set")
            .build();
    static Option opt_withoutFieldHiding = Option.builder("withoutFieldHiding")
            .desc("Disable hiding of taint fields via reflection")
            .build();
    static Option opt_withoutPropagation = Option.builder("withoutPropagation")
            .desc("Disable all tag propagation - still create method stubs and wrappers as per other options, but don't actually propagate tags")
            .build();
    static Option opt_enumPropagation = Option.builder("withEnumsByValue")
            .desc("Propagate tags to enums as if each enum were a value (not a reference) through the Enum.valueOf method")
            .build();
    static Option opt_unboxAcmpEq = Option.builder("forceUnboxAcmpEq")
            .desc("At each object equality comparison, ensure that all operands are unboxed (and not boxed types, which may not pass the test)")
            .build();
    static Option opt_disableJumpOptimizations = Option.builder("disableJumpOptimizations")
            .desc("Do not optimize taint removal at jump calls")
            .build();
    static Option opt_readAndSaveBCI = Option.builder("readAndSaveBCIs")
            .desc("Read in and track the byte code index of every instruction during instrumentation")
            .build();
    static Option opt_serialization = Option.builder("serialization")
            .desc("Read and write taint tags through Java Serialization")
            .build();
    static Option opt_disableLocalsInfo = Option.builder("skipLocals")
            .desc("Do not output local variable debug tables for generated local variables (useful for avoiding warnings from D8)")
            .build();
    static Option opt_alwaysCheckForFrames = Option.builder("alwaysCheckForFrames")
            .desc("Always check to ensure that class files with version > Java 8 ACTUALLY have frames - useful for instrumenting android-targeting code that is compiled with Java 8 but without frames")
            .build();
    static Option opt_priorClassVisitor = Option.builder("priorClassVisitor")
            .hasArg()
            .desc("Specify the class name for a ClassVisitor class to be added to Phosphor's visitor chain before taint tracking is added to the class.")
            .build();
    static Option opt_implicitHeadersNoTracking = Option.builder("implicitHeadersNoTracking")
            .desc("Add method headers for doing implicit tracking, but don't actually propagate them")
            .build();
    static Option opt_reenableCaches = Option.builder("reenableCaches")
            .desc("Prevent Phosphor from disabling caches.")
            .build();
    static Option opt_bindingControl = Option.builder("bindingControlTracking")
            .desc("Enable tag propagation due to certain control flows where values are bound to a particular.")
            .build();
    static Option help = Option.builder("help")
            .desc("print this message")
            .build();
    static Option opt_withCCSinks = Option.builder("withCCSinks")
            .desc("Add ConfigCrusher sinks")
            .build();
    private static ClassFileTransformer addlTransformer;
    private static File rootOutputDir;
    private static long START;

    private Instrumenter() {
        // Prevents this class from being instantiated
    }

    public static void preAnalysis() {

    }

    public static void finishedAnalysis() {
        System.out.println("Analysis Completed: Beginning Instrumentation Phase");

    }

    public static boolean isCollection(String internalName) {
        try {
            Class c;
            if(TaintTrackingClassVisitor.IS_RUNTIME_INST && !internalName.startsWith("java/")) {
                return false;
            }
            if(loader == null) {
                c = Class.forName(internalName.replace("/", "."));
            } else {
                c = loader.loadClass(internalName.replace("/", "."));
            }
            if(java.util.Collection.class.isAssignableFrom(c)) {
                return true;
            }
        } catch(Throwable ex) {
            //
        }
        return false;
    }

    public static boolean isClassWithHashMapTag(String clazz) {
        return clazz.startsWith("java/lang/Boolean")
                || clazz.startsWith("java/lang/Character")
                || clazz.startsWith("java/lang/Byte")
                || clazz.startsWith("java/lang/Short");
    }

    public static boolean isIgnoredClass(String owner) {
        if(Configuration.taintTagFactory.isIgnoredClass(owner)) {
            return true;
        }
        return (Configuration.ADDL_IGNORE != null && StringUtils.startsWith(owner, Configuration.ADDL_IGNORE)) || StringUtils.startsWith(owner, "java/lang/Object") || StringUtils.startsWith(owner, "java/lang/Boolean") || StringUtils.startsWith(owner, "java/lang/Character")
                || StringUtils.startsWith(owner, "java/lang/Byte")
                || StringUtils.startsWith(owner, "java/lang/Short")
                || StringUtils.startsWith(owner, "org/jikesrvm") || StringUtils.startsWith(owner, "com/ibm/tuningfork") || StringUtils.startsWith(owner, "org/mmtk") || StringUtils.startsWith(owner, "org/vmmagic")
                || StringUtils.startsWith(owner, "java/lang/Number") || StringUtils.startsWith(owner, "java/lang/Comparable") || StringUtils.startsWith(owner, "java/lang/ref/SoftReference") || StringUtils.startsWith(owner, "java/lang/ref/Reference")
                // || StringUtils.startsWith(owner, "java/awt/image/BufferedImage")
                // || owner.equals("java/awt/Image")
                || (StringUtils.startsWith(owner, "edu/columbia/cs/psl/phosphor"))
                || (StringUtils.startsWith(owner, "edu/gmu/swe/phosphor/ignored"))
                || StringUtils.startsWith(owner, "sun/awt/image/codec/")
                || (StringUtils.startsWith(owner, "sun/reflect/Reflection")) //was on last
                || owner.equals("java/lang/reflect/Proxy") //was on last
                || StringUtils.startsWith(owner, "sun/reflection/annotation/AnnotationParser") //was on last
                || StringUtils.startsWith(owner, "sun/reflect/MethodAccessor") //was on last
                || StringUtils.startsWith(owner, "org/apache/jasper/runtime/JspSourceDependent")
                || StringUtils.startsWith(owner, "sun/reflect/ConstructorAccessor") //was on last
                || StringUtils.startsWith(owner, "sun/reflect/SerializationConstructorAccessor")
                || StringUtils.startsWith(owner, "sun/reflect/GeneratedMethodAccessor")
                || StringUtils.startsWith(owner, "sun/reflect/GeneratedConstructorAccessor")
                || StringUtils.startsWith(owner, "sun/reflect/GeneratedSerializationConstructor")
                || StringUtils.startsWith(owner, "sun/awt/image/codec/")
                || StringUtils.startsWith(owner, "java/lang/invoke/LambdaForm")
                || StringUtils.startsWith(owner, "java/lang/invoke/LambdaMetafactory")
                || StringUtils.startsWith(owner, "edu/columbia/cs/psl/phosphor/struct/TaintedWith")
                || StringUtils.startsWith(owner, "java/util/regex/HashDecompositions") //Huge constant array/hashmap
                || StringUtils.startsWith(owner, "java/lang/invoke/MethodHandle")
                || (StringUtils.startsWith(owner, "java/lang/invoke/BoundMethodHandle") && !StringUtils.startsWith(owner, "java/lang/invoke/BoundMethodHandle$Factory"))
                || StringUtils.startsWith(owner, "java/lang/invoke/DelegatingMethodHandle")
                || owner.equals("java/lang/invoke/DirectMethodHandle")
                || StringUtils.startsWith(owner, "java/util/function/Function")
                || owner.startsWith("edu/cmu/cs/mvelezce/cc");
    }

    public static void analyzeClass(InputStream is) {
        ClassReader cr;
        nTotal++;
        try {
            cr = new ClassReader(is);
            cr.accept(new ClassVisitor(Configuration.ASM_VERSION) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    ClassNode cn = new ClassNode();
                    cn.name = name;
                    cn.superName = superName;
                    cn.interfaces = new java.util.ArrayList<>(java.util.Arrays.asList(interfaces));
                    Instrumenter.classes.put(name, cn);
                }
            }, ClassReader.SKIP_CODE);
            is.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] instrumentClass(String path, InputStream is, boolean renameInterfaces) {
        try {
            // n is shared among threads, but is used only to provide progress feedback
            // Therefore, it's ok to increment it in a non-thread-safe way
            n++;
            if(n % 1000 == 0) {
                System.out.println("Processed: " + n + "/" + nTotal);
            }
            curPath = path;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            is.close();
            buffer.flush();
            PreMain.PCLoggingTransformer transformer = new PreMain.PCLoggingTransformer();
            byte[] ret = transformer.transform(Instrumenter.loader, path, null, null, buffer.toByteArray());
            if(addlTransformer != null) {
                byte[] ret2 = addlTransformer.transform(Instrumenter.loader, path, null, null, ret);
                if(ret2 != null) {
                    ret = ret2;
                }
            }
            curPath = null;
            return ret;
        } catch(Exception ex) {
            curPath = null;
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        START = System.currentTimeMillis();
        Options options = new Options();
        options.addOption(help);
        OptionGroup controlPropagationGroup = new OptionGroup()
                .addOption(opt_controlTrack)
                .addOption(opt_controlLightTrack)
                .addOption(opt_implicitHeadersNoTracking)
                .addOption(opt_bindingControl);
        options.addOptionGroup(controlPropagationGroup);
        options.addOption(opt_controlTrackExceptions);
        options.addOption(opt_withoutDataTrack);
        options.addOption(opt_trackArrayLengthTaints);
        options.addOption(opt_trackArrayIndexTaints);
        options.addOption(opt_withoutFieldHiding);
        options.addOption(opt_withoutPropagation);
        options.addOption(opt_enumPropagation);
        options.addOption(opt_unboxAcmpEq);
        options.addOption(opt_disableJumpOptimizations);
        options.addOption(opt_readAndSaveBCI);
        options.addOption(opt_serialization);
        options.addOption(opt_withoutBranchNotTaken);
        options.addOption(opt_disableLocalsInfo);
        options.addOption(opt_alwaysCheckForFrames);
        options.addOption(opt_priorClassVisitor);
        options.addOption(opt_reenableCaches);
        options.addOption(opt_withCCSinks);
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch(org.apache.commons.cli.ParseException exp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar phosphor.jar [OPTIONS] [input] [output]", options);
            System.err.println(exp.getMessage());
            if(exp.getMessage().contains("-multiTaint")) {
                System.err.println("Note: the -multiTaint option has been removed, and is now enabled by default (int tags no longer exist)");
            }
            return;
        }
        if(line.hasOption("help") || line.getArgs().length != 2) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar phosphor.jar [OPTIONS] [input] [output]", options);
            return;
        }
        Configuration.IMPLICIT_TRACKING = line.hasOption(opt_controlTrack.getOpt());
        Configuration.IMPLICIT_LIGHT_TRACKING = line.hasOption(opt_controlLightTrack.getOpt());
        Configuration.IMPLICIT_EXCEPTION_FLOW = line.hasOption(opt_controlTrackExceptions.getOpt());
        Configuration.DATAFLOW_TRACKING = !line.hasOption(opt_withoutDataTrack.getOpt());
        Configuration.ARRAY_LENGTH_TRACKING = line.hasOption(opt_trackArrayLengthTaints.getOpt());
        Configuration.WITHOUT_FIELD_HIDING = line.hasOption(opt_withoutFieldHiding.getOpt());
        Configuration.WITHOUT_PROPAGATION = line.hasOption(opt_withoutPropagation.getOpt());
        Configuration.WITH_ENUM_BY_VAL = line.hasOption(opt_enumPropagation.getOpt());
        Configuration.WITH_UNBOX_ACMPEQ = line.hasOption(opt_unboxAcmpEq.getOpt());
        Configuration.WITH_TAGS_FOR_JUMPS = line.hasOption(opt_disableJumpOptimizations.getOpt());
        Configuration.READ_AND_SAVE_BCI = line.hasOption(opt_readAndSaveBCI.getOpt());
        // Configuration.TAINT_THROUGH_SERIALIZATION = line.hasOption(opt_serialization.getOpt()); // Really needs to always be active
        Configuration.ARRAY_INDEX_TRACKING = line.hasOption(opt_trackArrayIndexTaints.getOpt());
        Configuration.WITHOUT_BRANCH_NOT_TAKEN = line.hasOption(opt_withoutBranchNotTaken.getOpt());
        Configuration.SKIP_LOCAL_VARIABLE_TABLE = line.hasOption(opt_disableLocalsInfo.getOpt());
        Configuration.ALWAYS_CHECK_FOR_FRAMES = line.hasOption(opt_alwaysCheckForFrames.getOpt());
        Configuration.WITH_CC_SINKS = line.hasOption(opt_withCCSinks.getOpt());
        Configuration.IMPLICIT_HEADERS_NO_TRACKING = line.hasOption(opt_implicitHeadersNoTracking.getOpt());
        Configuration.BINDING_CONTROL_FLOWS_ONLY = line.hasOption(opt_bindingControl.getOpt());
        Configuration.REENABLE_CACHES = line.hasOption(opt_reenableCaches.getOpt());
        String priorClassVisitorName = line.getOptionValue(opt_priorClassVisitor.getOpt());
        if(priorClassVisitorName != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends ClassVisitor> temp = (Class<? extends ClassVisitor>) Class.forName(priorClassVisitorName);
                Configuration.PRIOR_CLASS_VISITOR = temp;
            } catch(Exception e) {
                System.err.println("Failed to create specified prior class visitor: " + priorClassVisitorName);
            }
        }
        Configuration.init();
        if(Configuration.DATAFLOW_TRACKING) {
            System.out.println("Data flow tracking: enabled");
        } else {
            System.out.println("Data flow tracking: disabled");
        }
        if(Configuration.IMPLICIT_TRACKING) {
            System.out.println("Control flow tracking: enabled");
        } else {
            System.out.println("Control flow tracking: disabled");
        }
        if(Configuration.WITHOUT_BRANCH_NOT_TAKEN) {
            System.out.println("Branch not taken: disabled");
        } else {
            System.out.println("Branch not taken: enabled");
        }
        if(Configuration.WITH_CC_SINKS) {
            System.out.println("Adding ConfigCrusher sinks");
        }
        if(Configuration.IMPLICIT_HEADERS_NO_TRACKING) {
            System.out.println("Adding implicit headers, but not doing tracking in the body");
        }

        TaintTrackingClassVisitor.IS_RUNTIME_INST = false;
        ANALYZE_ONLY = true;
        System.out.println("Starting analysis");
        _main(line.getArgs());
        System.out.println("Analysis Completed: Beginning Instrumentation Phase");
        ANALYZE_ONLY = false;
        _main(line.getArgs());
        System.out.println("Done after " + (System.currentTimeMillis() - START) + " ms");
    }

    public static void _main(String[] args) {
        if(PreMain.DEBUG) {
            System.err.println("Warning: Debug output enabled (uses a lot of IO!)");
        }

        String outputFolder = args[1];
        rootOutputDir = new File(outputFolder);
        if(!rootOutputDir.exists()) {
            rootOutputDir.mkdir();
        }
        String inputFolder = args[0];

        // Setup the class loader
        final ArrayList<URL> urls = new ArrayList<URL>();
        Path input = FileSystems.getDefault().getPath(args[0]);
        try {
            if(Files.isDirectory(input)) {
                Files.walkFileTree(input, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if(file.getFileName().toString().endsWith(".jar")) {
                            urls.add(file.toUri().toURL());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else if(inputFolder.endsWith(".jar")) {
                urls.add(new File(inputFolder).toURI().toURL());
            }
        } catch(IOException e1) {
            e1.printStackTrace();
        }

        try {
            urls.add(new File(inputFolder).toURI().toURL());
        } catch(MalformedURLException e1) {
            e1.printStackTrace();
        }
        if(args.length == 3) {
            System.out.println("Using extra classpath file: " + args[2]);
            try {
                File f = new File(args[2]);
                if(f.exists() && f.isFile()) {
                    Scanner s = new Scanner(f);
                    while(s.hasNextLine()) {
                        urls.add(new File(s.nextLine()).getCanonicalFile().toURI().toURL());
                    }
                } else if(f.isDirectory()) {
                    urls.add(f.toURI().toURL());
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else if(args.length > 3) {
            for(int i = 2; i < args.length; i++) {
                File f = new File(args[i]);
                if(!f.exists()) {
                    System.err.println("Unable to read path " + args[i]);
                    System.exit(-1);
                }
                if(f.isDirectory() && !f.getAbsolutePath().endsWith("/")) {
                    f = new File(f.getAbsolutePath() + "/");
                }
                try {
                    urls.add(f.getCanonicalFile().toURI().toURL());
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        URL[] urlArray = new URL[urls.size()];
        urlArray = urls.toArray(urlArray);
        loader = new URLClassLoader(urlArray, Instrumenter.class.getClassLoader());
        PreMain.bigLoader = loader;

        final File f = new File(inputFolder);
        if(!f.exists()) {
            System.err.println("Unable to read path " + inputFolder);
            System.exit(-1);
        }

        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        LinkedList<Future> toWait = new LinkedList<>();

        if(f.isDirectory()) {
            toWait.addAll(processDirectory(f, rootOutputDir, true, executor));
        } else if(inputFolder.endsWith(".jar") || inputFolder.endsWith(".zip") || inputFolder.endsWith(".war")) {
            toWait.addAll(processZip(f, rootOutputDir, executor));
        } else if(inputFolder.endsWith(".class")) {
            toWait.addAll(processClass(f, rootOutputDir, executor));
        } else {
            System.err.println("Unknown type for path " + inputFolder);
            System.exit(-1);
        }

        while(!toWait.isEmpty()) {
            try {
                toWait.addAll((Collection<? extends Future>) toWait.removeFirst().get());
            } catch(InterruptedException e) {
                //
            } catch(ExecutionException e) {
                throw new Error(e);
            }
        }

        executor.shutdown();
        while(!executor.isTerminated()) {
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch(InterruptedException e) {
                //
            }
        }
    }

    private static List<Future<? extends Collection>> processClass(File f, final File outputDir, ExecutorService executor) {
        List<Future<? extends Collection>> ret = new LinkedList<>();
        try {
            final String name = f.getName();
            final InputStream is = new FileInputStream(f);
            if(ANALYZE_ONLY) {
                analyzeClass(is);
                is.close();
            } else {
                ret.add(executor.submit(new Callable<List>() {
                    @Override
                    public List call() throws Exception {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        FileOutputStream fos = new FileOutputStream(outputDir.getPath() + File.separator + name);
                        byte[] c = instrumentClass(outputDir.getAbsolutePath(), is, true);
                        is.close();
                        bos.write(c);
                        bos.writeTo(fos);
                        fos.close();
                        return new LinkedList();
                    }
                }));
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    private static LinkedList<Future> processDirectory(File f, File parentOutputDir, boolean isFirstLevel, ExecutorService executor) {
        LinkedList<Future> ret = new LinkedList<>();
        if(f.getName().equals(".AppleDouble")) {
            return ret;
        }
        final File thisOutputDir;
        if(isFirstLevel) {
            thisOutputDir = parentOutputDir;
        } else {
            thisOutputDir = new File(parentOutputDir.getAbsolutePath() + File.separator + f.getName());
            thisOutputDir.mkdir();
        }
        for(final File fi : f.listFiles()) {
            if(fi.isDirectory()) {
                ret.addAll(processDirectory(fi, thisOutputDir, false, executor));
            } else if(fi.getName().endsWith(".class")) {
                ret.addAll(processClass(fi, thisOutputDir, executor));
            } else if(fi.getName().endsWith(".jar") || fi.getName().endsWith(".zip") || fi.getName().endsWith(".war")) {
                ret.addAll(processZip(fi, thisOutputDir, executor));
            } else {
                File dest = new File(thisOutputDir.getPath() + File.separator + fi.getName());
                FileChannel source = null;
                FileChannel destination = null;

                try {
                    source = new FileInputStream(fi).getChannel();
                    destination = new FileOutputStream(dest).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    if(fi.canExecute()) {
                        dest.setExecutable(true);
                    }
                    if(fi.canRead()) {
                        dest.setReadable(true);
                    }
                    if(fi.canWrite()) {
                        dest.setWritable(true);
                    }
                } catch(Exception ex) {
                    System.err.println("error copying file " + fi);
                    ex.printStackTrace();
                } finally {
                    if(source != null) {
                        try {
                            source.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(destination != null) {
                        try {
                            destination.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return ret;
    }

    public static boolean isIgnoredFromControlTrack(String className, String name) {
        return (className.equals("java/nio/charset/Charset")
                || className.equals("java/lang/StringCoding")
                || className.equals("java/nio/charset/CharsetEncoder")
                || className.equals("java/nio/charset/CharsetDecoder"))
                && !name.equals("<clinit>") && !name.equals("<init>");
    }

    /**
     * Handles Jar files, Zip files and War files.
     */
    public static LinkedList<Future> processZip(final File f, File outputDir, ExecutorService executor) {
        return _processZip(f, outputDir, executor, false);
    }

    private static LinkedList<Future> _processZip(final File f, File outputDir, ExecutorService executor, boolean unCompressed) {
        try {
            LinkedList<Future<Result>> ret = new LinkedList<>();
            final ZipFile zip = new ZipFile(f);
            ZipOutputStream zos;
            zos = new ZipOutputStream(new FileOutputStream(outputDir.getPath() + File.separator + f.getName()));
            if(unCompressed) {
                zos.setLevel(ZipOutputStream.STORED);
            }
            java.util.Enumeration<? extends ZipEntry> entries = zip.entries();
            while(entries.hasMoreElements()) {
                final ZipEntry e = entries.nextElement();

                if(e.getName().endsWith(".class")) {
                    if(ANALYZE_ONLY) {
                        analyzeClass(zip.getInputStream(e));
                    } else {
                        ret.add(executor.submit(new Callable<Result>() {
                            @Override
                            public Result call() throws Exception {
                                Result ret = new Result();
                                ret.e = e;
                                ret.buf = instrumentClass(f.getAbsolutePath(), zip.getInputStream(e), true);
                                return ret;
                            }
                        }));
                    }
                } else if(e.getName().endsWith(".jar")) {
                    ZipEntry outEntry = new ZipEntry(e.getName());
                    Random r = new Random();
                    String markFileName = Long.toOctalString(System.currentTimeMillis())
                            + Integer.toOctalString(r.nextInt(10000))
                            + e.getName().replace("/", "");
                    File tmp = new File("/tmp/" + markFileName);
                    if(tmp.exists()) {
                        tmp.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(tmp);
                    byte[] buf = new byte[1024];
                    int len;
                    InputStream is = zip.getInputStream(e);
                    while((len = is.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    is.close();
                    fos.close();

                    File tmp2 = new File("/tmp/tmp2");
                    if(!tmp2.exists()) {
                        tmp2.mkdir();
                    }
                    _processZip(tmp, tmp2, executor, true);
                    tmp.delete();

                    outEntry.setMethod(ZipEntry.STORED);
                    Path newFile = Paths.get("/tmp/tmp2/" + markFileName);

                    outEntry.setSize(Files.size(newFile));
                    CRC32 crc = new CRC32();
                    crc.update(Files.readAllBytes(newFile));
                    outEntry.setCrc(crc.getValue());
                    zos.putNextEntry(outEntry);
                    is = new FileInputStream("/tmp/tmp2/" + markFileName);
                    byte[] buffer = new byte[1024];
                    while(true) {
                        int count = is.read(buffer);
                        if(count == -1) {
                            break;
                        }
                        zos.write(buffer, 0, count);
                    }
                    is.close();
                    zos.closeEntry();
                } else {
                    ZipEntry outEntry = new ZipEntry(e.getName());
                    if(e.isDirectory()) {
                        try {
                            zos.putNextEntry(outEntry);
                            zos.closeEntry();
                        } catch(ZipException exxxx) {
                            System.out.println("Ignoring exception: " + exxxx.getMessage());
                        }
                    } else if(!e.getName().startsWith("META-INF")
                            || (!e.getName().endsWith(".SF")
                            && !e.getName().endsWith(".RSA"))) {
                        if(e.getName().equals("META-INF/MANIFEST.MF")) {
                            Scanner s = new Scanner(zip.getInputStream(e));
                            zos.putNextEntry(outEntry);

                            String curPair = "";
                            while(s.hasNextLine()) {
                                String line = s.nextLine();
                                if(line.equals("")) {
                                    curPair += "\n";
                                    if(!curPair.contains("SHA1-Digest:")) {
                                        zos.write(curPair.getBytes());
                                    }
                                    curPair = "";
                                } else {
                                    curPair += line + "\n";
                                }
                            }
                            s.close();
                            // Jar file is different from Zip file. :)
                            if(f.getName().endsWith(".zip")) {
                                zos.write("\n".getBytes());
                            }
                            zos.closeEntry();
                        } else {
                            try {
                                zos.putNextEntry(outEntry);
                                InputStream is = zip.getInputStream(e);
                                byte[] buffer = new byte[1024];
                                while(true) {
                                    int count = is.read(buffer);
                                    if(count == -1) {
                                        break;
                                    }
                                    zos.write(buffer, 0, count);
                                }
                                is.close();
                                zos.closeEntry();
                            } catch(ZipException ex) {
                                if(!ex.getMessage().contains("duplicate entry")) {
                                    ex.printStackTrace();
                                    System.out.println("Ignoring above warning from improper source zip...");
                                }
                            }
                        }
                    }
                }
            }
            for(Future<Result> fr : ret) {
                Result r;
                while(true) {
                    try {
                        r = fr.get();
                        break;
                    } catch(InterruptedException e) {
                        //
                    }
                }
                try {
                    ZipEntry outEntry = new ZipEntry(r.e.getName());
                    zos.putNextEntry(outEntry);

                    byte[] clazz = r.buf;
                    if(clazz == null) {
                        System.out.println("Failed to instrument " + r.e.getName() + " in " + f.getName());
                        InputStream is = zip.getInputStream(r.e);
                        byte[] buffer = new byte[1024];
                        while(true) {
                            int count = is.read(buffer);
                            if(count == -1) {
                                break;
                            }
                            zos.write(buffer, 0, count);
                        }
                        is.close();
                    } else {
                        zos.write(clazz);
                    }
                    zos.closeEntry();
                } catch(ZipException ex) {
                    ex.printStackTrace();
                }
            }
            zos.close();
            zip.close();
        } catch(Exception e) {
            System.err.println("Unable to process zip/jar: " + f.getAbsolutePath());
            e.printStackTrace();
            File dest = new File(outputDir.getPath() + File.separator + f.getName());
            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(f).getChannel();
                destination = new FileOutputStream(dest).getChannel();
                destination.transferFrom(source, 0, source.size());
            } catch(Exception ex) {
                System.err.println("Unable to copy zip/jar: " + f.getAbsolutePath());
                ex.printStackTrace();
            } finally {
                if(source != null) {
                    try {
                        source.close();
                    } catch(IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if(destination != null) {
                    try {
                        destination.close();
                    } catch(IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        return new LinkedList<>();
    }

    public static boolean isIgnoredMethod(String owner, String name, String desc) {
        if(name.equals("wait") && desc.equals("(J)V")) {
            return true;
        }
        if(name.equals("wait") && desc.equals("(JI)V")) {
            return true;
        }
        return Configuration.IMPLICIT_TRACKING && owner.equals("java/lang/invoke/MethodHandle")
                && ((name.equals("invoke") || name.equals("invokeBasic") || name.startsWith("linkTo")));
    }

    public static boolean isUninstrumentedField(String owner, String name) {
        return owner.equals("sun/java2d/cmm/lcms/LCMSImageLayout") && name.equals("dataArray");
    }

    /* Returns the class node associated with the specified class name or null if none exists and a new one could not
     * successfully be created for the class name. */
    public static ClassNode getClassNode(String className) {
        ClassNode cn = classes.get(className);
        if(cn == null) {
            // Class was loaded before ClassSupertypeReadingTransformer was added
            return tryToAddClassNode(className);
        } else {
            return cn;
        }
    }

    /* Attempts to create a ClassNode populated with supertype information for this class. */
    private static ClassNode tryToAddClassNode(String className) {
        try {
            String resource = className + ".class";
            InputStream is = ClassLoader.getSystemResourceAsStream(resource);
            if(is == null) {
                return null;
            }
            ClassReader cr = new ClassReader(is);
            cr.accept(new ClassVisitor(Configuration.ASM_VERSION) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    ClassNode cn = new ClassNode();
                    cn.name = name;
                    cn.superName = superName;
                    cn.interfaces = new java.util.ArrayList<>(java.util.Arrays.asList(interfaces));
                    classes.put(name, cn);
                }
            }, ClassReader.SKIP_CODE);
            is.close();
            return classes.get(className);
        } catch(Exception e) {
            return null;
        }
    }

    private static class Result {
        ZipEntry e;
        byte[] buf;
    }
}
