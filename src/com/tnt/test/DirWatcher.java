package com.tnt.test;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * java7文件夹监控程序的封装。
 * version: 2012_04_03
 */
public final class DirWatcher {
	public static interface WatchEventHandler {
		void handleEvent(KeyState event);
	}

	/**
	 * 记录操作类型
	 * 
	 */
	static enum OperationType {
		/**
		 * @author LC 调整
		 */
		Modify {
			@Override
			public String toString() {
				return "Modify";
			}
		},
		/**
		 * @author LC 新建
		 */
		Create {
			@Override
			public String toString() {
				return "Create";
			}
		},
		/**
		 * @author LC 删除
		 */
		Delete {
			@Override
			public String toString() {
				return "Delete";
			}
		},
		/**
		 * @author LC 空事件
		 */
		Null {
			@Override
			public String toString() {
				return "Null";
			}
		},
		/**
		 * @author LC 重命名
		 */
		Rename {
			@Override
			public String toString() {
				return "Rename";
			}
		},
		/**
		 * @author LC 没用到
		 */
		Move {
			@Override
			public String toString() {
				return "Move";
			}
		}
	}

	/**
	 * 记录{@link WatchKey}及事件的状态类
	 */
	class KeyState {
		/**
		 * 事件发生的路径或者{@link WatchKey}对应的路径
		 */
		public Path path;
		/**
		 * {@link WatchKey}上一步操作的路径或者重命名事件中的重命名后的名称
		 */
		public Path exPath;
		/**
		 * 事件的类型
		 */
		public OperationType opType = OperationType.Null;
		/**
		 * {@link WatchKey}的等级或者引发事件的{@link WatchKey}的等级
		 */
		public int level;
		/**
		 * 事件发生的事件
		 */
		public long opTime = -1;

		/**
		 * @param path
		 *            事件发生的路径或者{@link WatchKey}对应的路径
		 * @param level
		 *            {@link WatchKey}的等级或者引发事件的{@link WatchKey}的等级
		 */
		public KeyState(Path path, int level) {
			this.path = path;
			this.level = level;
		}

		/**
		 * @param path
		 *            事件发生的路径或者{@link WatchKey}对应的路径
		 * @param opType
		 *            操作的类型
		 * @param level
		 *            {@link WatchKey}的等级或者引发事件的{@link WatchKey}的等级
		 */
		public KeyState(Path path, OperationType opType, int level) {
			this.path = path;
			this.level = level;
			this.opType = opType;
		}

		/**
		 * @param path
		 *            事件发生的路径或者{@link WatchKey}对应的路径
		 * @param opType
		 *            操作的类型
		 * @param level
		 *            {@link WatchKey}的等级或者引发事件的{@link WatchKey}的等级
		 * @param opTime
		 *            事件发生的时间
		 */
		public KeyState(Path path, OperationType opType, int level, long opTime) {
			super();
			this.path = path;
			this.opType = opType;
			this.level = level;
			this.opTime = opTime;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("opType=" + opType);
			sb.append("\topTime=" + opTime);
			sb.append("\tlevel=" + level);
			if (path != null)
				sb.append("\tpath=" + path.normalize());
			if (exPath != null)
				sb.append("\texPath=" + exPath.normalize());

			return sb.toString();
		}

	}

	private Path pathToMonitor;

	private WatchService ws;
	/**
	 * 是否监控子目录树
	 */
	private boolean watchSubDir = false;
	/**
	 * 存储键值对应的路径等信息
	 */
	private HashMap<WatchKey, KeyState> keyMap = new HashMap<WatchKey, KeyState>();
	/**
	 * 监控的最大层级
	 */
	private int maxLevel = 100;
	/**
	 * 是否输出调试信息
	 */
	static boolean debug = true;
	/**
	 * 处理事件的函数类
	 */
	private WatchEventHandler eventHandler;

	private static final WatchEventHandler nullHandler = new WatchEventHandler() {

		@Override
		public void handleEvent(KeyState event) {
			if (!debug) {
				System.out.println(event);
				System.out.println();
			}
		}
	};

	/**
	 * 演示用
	 * 
	 * @param pathToMonitor
	 * @param watchSubDir
	 */
	public DirWatcher(Path pathToMonitor, boolean watchSubDir) {
		this.pathToMonitor = pathToMonitor;
		this.watchSubDir = watchSubDir;
		try {
			ws = FileSystems.getDefault().newWatchService();
			if (watchSubDir)
				registerAll(pathToMonitor, maxLevel);
			else
				register(pathToMonitor, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		eventHandler = nullHandler;

	}

	public DirWatcher(Path pathToMonitor, boolean watchSubDir, int maxLevel,
			WatchEventHandler eventHandler) {
		super();
		this.pathToMonitor = pathToMonitor;
		this.watchSubDir = watchSubDir;
		this.maxLevel = maxLevel;
		this.eventHandler = eventHandler == null ? eventHandler : nullHandler;
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * 注册{@link WatchService}
	 * 
	 * @param pathToMonitor
	 *            需要被监控的目录
	 * @param level
	 *            相对于最上层的要监控的目录的等级，如d:\test为第0级，则d:\test\folder则为1级
	 * @throws IOException
	 */
	private void register(Path pathToMonitor, int level) throws IOException {
		WatchKey key = pathToMonitor.register(ws, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		//		System.out.println(key + "\t注册key," + dir.toFile().getAbsolutePath());
		if (debug) {
			KeyState prev = keyMap.get(key);
			if (prev == null) {
				System.out.format("-->\tregister: %s\tregisted level=%d\n",
						pathToMonitor, level);
			} else {
				if (!pathToMonitor.equals(prev.path)) {
					System.out.format(
							"-->\tupdate: %s -> %s\tregisted level=%d\n", prev,
							pathToMonitor, level);
				}
			}
		}
		KeyState prev = keyMap.get(key);
		if (prev == null)
			keyMap.put(key, new KeyState(pathToMonitor, level));
		else {
			prev.path = pathToMonitor;
			prev.level = level;
		}
	}

	/**
	 * 监控一个目录及其子目录
	 * 
	 * @param curPath
	 *            要被监控的目录
	 * @param maxLevel
	 *            监控的等级数量
	 * @throws IOException
	 */
	private void registerAll(final Path curPath, final int maxLevel)
			throws IOException {
		registerAll(curPath, 0, maxLevel);
	}

	/**
	 * 监控一个目录及其子目录
	 * 
	 * @param curPath
	 *            要被监控的目录
	 * @param curLevel
	 *            当前目录相对于最上层的要监控的目录的等级，如d:\test为第0级，则d:\test\folder则为1级
	 * @param maxLevel
	 *            监控的等级数量，curLevel<maxLevel时，才进行监控
	 * @throws IOException
	 */
	private void registerAll(final Path curPath, final int curLevel,
			final int maxLevel) throws IOException {

		Files.walkFileTree(curPath, new SimpleFileVisitor<Path>() {
			int curLv = curLevel;

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				if (curLv < maxLevel)
					register(dir, curLv);
				++curLv;
				if (curLv < maxLevel) {
					return FileVisitResult.CONTINUE;
				} else {
					return FileVisitResult.SKIP_SUBTREE;
				}
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				--curLv;
				return super.postVisitDirectory(dir, exc);
			}
		});
	}

	/**
	 * 开始监控，事件的处理分为两类
	 * <p>
	 * 1、对重命名或新增的文件夹的监控，由Oracle的示例完成。
	 * <p>
	 * 2、文件夹、文件的删除、重命名、新建事件；对于这类事件，Java7处理的不好，会有重复的，对此进行了封装。原理是根据事件发生的顺序及时间间隔来判断
	 * ，如同一个key的删除接着一个新建实际上为重命名事件
	 * <p>
	 * 存在的问题：由于Java7系统函数的问题，当把一个含有文件的目录拷贝到监控中的目录时，有些文件无法扫描到！！！！（子目录的监控没有问题）
	 * 
	 */
	public void startMonitorDir() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					// 等待监控的事件的发生
					WatchKey key;
					try {
						key = ws.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}

					//事件的有效性检测
					KeyState stateOrigin = keyMap.get(key);
					Path dir = stateOrigin.path;
					if (dir == null) {
						System.err.println("WatchKey not recognized!!");
					}

					//
					int keyEventCount = 0;
					ArrayList<KeyState> eventList = new ArrayList<KeyState>();
					for (WatchEvent<?> event : key.pollEvents()) {
						Kind<?> kind = event.kind();
						// 遇到了不关心的事件
						if (kind == OVERFLOW) {
							continue;
						}
						++keyEventCount;

						// 获取文件路径
						WatchEvent<Path> ev = cast(event);
						Path name = ev.context();//相对路径
						Path child = dir.resolve(name);//绝对路径

						//增加到事件队列中
						KeyState eventState = new KeyState(child,
								stateOrigin.level);//不使用level字段
						eventState.opTime = System.currentTimeMillis();
						if (kind == ENTRY_CREATE) {
							eventState.opType = OperationType.Create;
						} else if (kind == ENTRY_DELETE) {
							eventState.opType = OperationType.Delete;
						} else if (kind == ENTRY_MODIFY)
							eventState.opType = OperationType.Modify;
						eventList.add(eventState);

						//事件的信息
						if (debug)
							System.out.format("%d:%s: %s\n", keyEventCount,
									event.kind().name(), child);

						// 如果有新的子目录被创建(或者其他目录更名为该目录)，则加入新的子目录到监控列表中
						if (watchSubDir && (kind == ENTRY_CREATE)) {
							try {
								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									if (debug)
										System.out.println(key + "\t原始key,"
												+ keyMap.get(key));
									registerAll(child, stateOrigin.level + 1,
											maxLevel);
								}
							} catch (IOException e) {
								//e.printStackTrace();
							}
						}
					}

					//分析事件队列，获取正确的事件
					if (eventList.size() == 1) {
						KeyState state = eventList.get(0);
						processEvent(key, state);
					} else if (eventList.size() > 1) {

						KeyState eventStart;

						final int size = eventList.size();
						for (int i = 0; i < size; i++) {
							eventStart = eventList.get(i);
							if (i + 1 < size) {
								KeyState eventEnd = eventList.get(i + 1);
								//重命名，先删除再创建
								if (eventStart.opType == OperationType.Delete
										&& eventEnd.opType == OperationType.Create) {
									eventStart.opType = OperationType.Rename;
									eventStart.exPath = eventEnd.path;
									processEvent(key, eventStart);
									++i;
									for (int j = i + 1; j < size; j++) {//过滤掉紧跟的modify事件
										KeyState tmp = eventList.get(j);
										if (tmp.path.equals(eventStart.exPath))
											i++;
									}
								} else {
									processEvent(key, eventStart);
									for (int j = i + 1; j < size; j++) {//过滤掉紧跟的modify事件
										KeyState tmp = eventList.get(j);
										if (tmp.path.equals(eventStart.path))
											i++;
									}

								}

							} else
								processEvent(key, eventStart);

							//							if(i+1>=size||evtList.get(i+1).lastOpType!=)

						}

					}
					if (debug)
						System.out.println();
					// reset key and remove from set if directory no longer accessible
					boolean valid = key.reset();
					if (!valid) {
						keyMap.remove(key);
						// all directories are inaccessible
						if (keyMap.isEmpty()) {
							break;
						}
					}

				}
			}

		}).start();
	}

	/**
	 * 处理事件，仍需要进行过滤
	 * 
	 * @param key
	 * @param event
	 */
	private void processEvent(WatchKey key, KeyState event) {
		KeyState keyState = keyMap.get(key);
		if (event.opType != OperationType.Modify
				|| !event.path.equals(keyState.exPath)
				|| keyState.opTime - event.opTime > 200) {//同一文件的两个modify时间过短，过滤掉一个（之前也有过滤，但中间可能插入了文件夹的modify事件）；注意keyState中exPath是事件记录，path是监控的文件夹路径

			if (event.opType != OperationType.Modify
					|| !Files
							.isDirectory(event.path, LinkOption.NOFOLLOW_LINKS)) {//过滤文件夹的modify事件，毕竟文件夹modify是因为文件夹下的文件的改变，已经有对应事件了！！！当然，理论上应该再检测一下文件夹是否处于监控列表中，懒得写了，反正都打算用jnotify了！！！
				if (debug)
					System.out.println("key=" + key.hashCode() + "\t" + event);
				eventHandler.handleEvent(event);
			}
		}
		keyState.exPath = event.opType == OperationType.Rename ? event.exPath
				: event.path;//作为上一步操作的文件进行记录
		keyState.opTime = event.opTime;
	}

	public static void main(String[] args) {
		DirWatcher watcher = new DirWatcher(Paths.get("C:\\to1"), true);
		watcher.startMonitorDir();
	}
}

