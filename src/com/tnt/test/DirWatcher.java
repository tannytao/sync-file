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
 * java7�ļ��м�س���ķ�װ��
 * version: 2012_04_03
 */
public final class DirWatcher {
	public static interface WatchEventHandler {
		void handleEvent(KeyState event);
	}

	/**
	 * ��¼��������
	 * 
	 */
	static enum OperationType {
		/**
		 * @author LC ����
		 */
		Modify {
			@Override
			public String toString() {
				return "Modify";
			}
		},
		/**
		 * @author LC �½�
		 */
		Create {
			@Override
			public String toString() {
				return "Create";
			}
		},
		/**
		 * @author LC ɾ��
		 */
		Delete {
			@Override
			public String toString() {
				return "Delete";
			}
		},
		/**
		 * @author LC ���¼�
		 */
		Null {
			@Override
			public String toString() {
				return "Null";
			}
		},
		/**
		 * @author LC ������
		 */
		Rename {
			@Override
			public String toString() {
				return "Rename";
			}
		},
		/**
		 * @author LC û�õ�
		 */
		Move {
			@Override
			public String toString() {
				return "Move";
			}
		}
	}

	/**
	 * ��¼{@link WatchKey}���¼���״̬��
	 */
	class KeyState {
		/**
		 * �¼�������·������{@link WatchKey}��Ӧ��·��
		 */
		public Path path;
		/**
		 * {@link WatchKey}��һ��������·�������������¼��е��������������
		 */
		public Path exPath;
		/**
		 * �¼�������
		 */
		public OperationType opType = OperationType.Null;
		/**
		 * {@link WatchKey}�ĵȼ����������¼���{@link WatchKey}�ĵȼ�
		 */
		public int level;
		/**
		 * �¼��������¼�
		 */
		public long opTime = -1;

		/**
		 * @param path
		 *            �¼�������·������{@link WatchKey}��Ӧ��·��
		 * @param level
		 *            {@link WatchKey}�ĵȼ����������¼���{@link WatchKey}�ĵȼ�
		 */
		public KeyState(Path path, int level) {
			this.path = path;
			this.level = level;
		}

		/**
		 * @param path
		 *            �¼�������·������{@link WatchKey}��Ӧ��·��
		 * @param opType
		 *            ����������
		 * @param level
		 *            {@link WatchKey}�ĵȼ����������¼���{@link WatchKey}�ĵȼ�
		 */
		public KeyState(Path path, OperationType opType, int level) {
			this.path = path;
			this.level = level;
			this.opType = opType;
		}

		/**
		 * @param path
		 *            �¼�������·������{@link WatchKey}��Ӧ��·��
		 * @param opType
		 *            ����������
		 * @param level
		 *            {@link WatchKey}�ĵȼ����������¼���{@link WatchKey}�ĵȼ�
		 * @param opTime
		 *            �¼�������ʱ��
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
	 * �Ƿ�����Ŀ¼��
	 */
	private boolean watchSubDir = false;
	/**
	 * �洢��ֵ��Ӧ��·������Ϣ
	 */
	private HashMap<WatchKey, KeyState> keyMap = new HashMap<WatchKey, KeyState>();
	/**
	 * ��ص����㼶
	 */
	private int maxLevel = 100;
	/**
	 * �Ƿ����������Ϣ
	 */
	static boolean debug = true;
	/**
	 * �����¼��ĺ�����
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
	 * ��ʾ��
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
	 * ע��{@link WatchService}
	 * 
	 * @param pathToMonitor
	 *            ��Ҫ����ص�Ŀ¼
	 * @param level
	 *            ��������ϲ��Ҫ��ص�Ŀ¼�ĵȼ�����d:\testΪ��0������d:\test\folder��Ϊ1��
	 * @throws IOException
	 */
	private void register(Path pathToMonitor, int level) throws IOException {
		WatchKey key = pathToMonitor.register(ws, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		//		System.out.println(key + "\tע��key," + dir.toFile().getAbsolutePath());
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
	 * ���һ��Ŀ¼������Ŀ¼
	 * 
	 * @param curPath
	 *            Ҫ����ص�Ŀ¼
	 * @param maxLevel
	 *            ��صĵȼ�����
	 * @throws IOException
	 */
	private void registerAll(final Path curPath, final int maxLevel)
			throws IOException {
		registerAll(curPath, 0, maxLevel);
	}

	/**
	 * ���һ��Ŀ¼������Ŀ¼
	 * 
	 * @param curPath
	 *            Ҫ����ص�Ŀ¼
	 * @param curLevel
	 *            ��ǰĿ¼��������ϲ��Ҫ��ص�Ŀ¼�ĵȼ�����d:\testΪ��0������d:\test\folder��Ϊ1��
	 * @param maxLevel
	 *            ��صĵȼ�������curLevel<maxLevelʱ���Ž��м��
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
	 * ��ʼ��أ��¼��Ĵ����Ϊ����
	 * <p>
	 * 1�������������������ļ��еļ�أ���Oracle��ʾ����ɡ�
	 * <p>
	 * 2���ļ��С��ļ���ɾ�������������½��¼������������¼���Java7����Ĳ��ã������ظ��ģ��Դ˽����˷�װ��ԭ���Ǹ����¼�������˳��ʱ�������ж�
	 * ����ͬһ��key��ɾ������һ���½�ʵ����Ϊ�������¼�
	 * <p>
	 * ���ڵ����⣺����Java7ϵͳ���������⣬����һ�������ļ���Ŀ¼����������е�Ŀ¼ʱ����Щ�ļ��޷�ɨ�赽������������Ŀ¼�ļ��û�����⣩
	 * 
	 */
	public void startMonitorDir() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					// �ȴ���ص��¼��ķ���
					WatchKey key;
					try {
						key = ws.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}

					//�¼�����Ч�Լ��
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
						// �����˲����ĵ��¼�
						if (kind == OVERFLOW) {
							continue;
						}
						++keyEventCount;

						// ��ȡ�ļ�·��
						WatchEvent<Path> ev = cast(event);
						Path name = ev.context();//���·��
						Path child = dir.resolve(name);//����·��

						//���ӵ��¼�������
						KeyState eventState = new KeyState(child,
								stateOrigin.level);//��ʹ��level�ֶ�
						eventState.opTime = System.currentTimeMillis();
						if (kind == ENTRY_CREATE) {
							eventState.opType = OperationType.Create;
						} else if (kind == ENTRY_DELETE) {
							eventState.opType = OperationType.Delete;
						} else if (kind == ENTRY_MODIFY)
							eventState.opType = OperationType.Modify;
						eventList.add(eventState);

						//�¼�����Ϣ
						if (debug)
							System.out.format("%d:%s: %s\n", keyEventCount,
									event.kind().name(), child);

						// ������µ���Ŀ¼������(��������Ŀ¼����Ϊ��Ŀ¼)��������µ���Ŀ¼������б���
						if (watchSubDir && (kind == ENTRY_CREATE)) {
							try {
								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									if (debug)
										System.out.println(key + "\tԭʼkey,"
												+ keyMap.get(key));
									registerAll(child, stateOrigin.level + 1,
											maxLevel);
								}
							} catch (IOException e) {
								//e.printStackTrace();
							}
						}
					}

					//�����¼����У���ȡ��ȷ���¼�
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
								//����������ɾ���ٴ���
								if (eventStart.opType == OperationType.Delete
										&& eventEnd.opType == OperationType.Create) {
									eventStart.opType = OperationType.Rename;
									eventStart.exPath = eventEnd.path;
									processEvent(key, eventStart);
									++i;
									for (int j = i + 1; j < size; j++) {//���˵�������modify�¼�
										KeyState tmp = eventList.get(j);
										if (tmp.path.equals(eventStart.exPath))
											i++;
									}
								} else {
									processEvent(key, eventStart);
									for (int j = i + 1; j < size; j++) {//���˵�������modify�¼�
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
	 * �����¼�������Ҫ���й���
	 * 
	 * @param key
	 * @param event
	 */
	private void processEvent(WatchKey key, KeyState event) {
		KeyState keyState = keyMap.get(key);
		if (event.opType != OperationType.Modify
				|| !event.path.equals(keyState.exPath)
				|| keyState.opTime - event.opTime > 200) {//ͬһ�ļ�������modifyʱ����̣����˵�һ����֮ǰҲ�й��ˣ����м���ܲ������ļ��е�modify�¼�����ע��keyState��exPath���¼���¼��path�Ǽ�ص��ļ���·��

			if (event.opType != OperationType.Modify
					|| !Files
							.isDirectory(event.path, LinkOption.NOFOLLOW_LINKS)) {//�����ļ��е�modify�¼����Ͼ��ļ���modify����Ϊ�ļ����µ��ļ��ĸı䣬�Ѿ��ж�Ӧ�¼��ˣ�������Ȼ��������Ӧ���ټ��һ���ļ����Ƿ��ڼ���б��У�����д�ˣ�������������jnotify�ˣ�����
				if (debug)
					System.out.println("key=" + key.hashCode() + "\t" + event);
				eventHandler.handleEvent(event);
			}
		}
		keyState.exPath = event.opType == OperationType.Rename ? event.exPath
				: event.path;//��Ϊ��һ���������ļ����м�¼
		keyState.opTime = event.opTime;
	}

	public static void main(String[] args) {
		DirWatcher watcher = new DirWatcher(Paths.get("C:\\to1"), true);
		watcher.startMonitorDir();
	}
}

