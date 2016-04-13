package com.update;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.DND;

public class UpdateApp {

	protected static final String DOWNLOAD_HTTP = "http://yun.baidu.com/s/1hq2LD4K";
	protected Shell shell;
	private Text text;
	private Text txt_msg;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UpdateApp window = new UpdateApp();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(SWT.MIN|SWT.CLOSE);
		shell.setSize(510, 338);
		shell.setText("更新(请自行备份)");
		shell.setLayout(new GridLayout(4, false));
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("帮助");
		
		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);
		
		MenuItem mntmNewItem = new MenuItem(menu_1, SWT.NONE);
		mntmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			        try {
			            Runtime.getRuntime().exec("cmd /k start " + DOWNLOAD_HTTP);
			        } catch (Exception ex) {
			        	txt_msg.append("打开网盘失败，"+ex);
			        }
			}
		});
		mntmNewItem.setText("更新包下载");
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("更新程序文件夹");
		
		text = new Text(shell, SWT.BORDER);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 217;
		text.setLayoutData(gd_text);
		
		Button button = new Button(shell, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.SINGLE);
				String open = directoryDialog.open();
				if (open==null||open.isEmpty()) {
					return;
				}
				text.setText(open);
			}
		});
		button.setText("...");
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						txt_msg.setText("正在更新，请稍后。。。\n");
						btnNewButton.setEnabled(false);
						update(text.getText());
						txt_msg.append("更新完成");
						btnNewButton.setEnabled(true);
					}
				});
			}
		});
		btnNewButton.setText("更新");
		
		txt_msg = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txt_msg.setEditable(false);
		txt_msg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		DropTarget dropTarget = new DropTarget(txt_msg, DND.DROP_MOVE);
		Transfer[] transfer = new Transfer[]{FileTransfer.getInstance()};
		dropTarget.setTransfer(transfer);
		dropTarget.addDropListener(new DropTargetAdapter() {
			
			
			@Override
			public void drop(DropTargetEvent e) {
				String[] ss=(String[]) e.data;
				if (ss!=null&&ss.length==1) {
					text.setText(ss[0]);
				}
			}
		});

	}

	protected void update(String path) {
		try {
			if (!new File(path+"\\bin").exists()) {
				return;
			}
			String src = System.getProperty("user.dir");
			String[] files=new String[]{"lib","native","jre\\carparklib"};
			for (int i = 0; i < files.length; i++) {
				File file = new File(src+File.separator+files[i]);
				if (!file.exists()) {
					continue;
				}
				boolean delete = deleteDir(file);
				if (!delete) {
					txt_msg.append("更新失败，请关闭程序在更新");
					return;
				}else{
					txt_msg.append("删除文件："+file+"成功\n");
				}
			}
			
			String srcBin=path;
			File[] listFiles = new File(srcBin).listFiles();
			
			for (File file : listFiles) {
				txt_msg.append("正在更新文件："+file+"\n");
				String parent = new File(System.getProperty("user.dir")).getParent();
				if (file.isDirectory()) {
					File[] listFiles2 = file.listFiles();
					for (File file2 : listFiles2) {
						if (!file2.isDirectory()) {
							continue;
						}
						if (file2.getName().equals("temp")||file2.getName().equals("img")||file2.getName().equals("update")||file2.getName().equals("log")) {
							continue;
						}
						String destDirName=parent+"\\bin\\"+file2.getName();
						if (file2.getName().equals("jre")) {
							File filelib = new File(file2.getPath()+File.separator+"carparklib");
							if (filelib.exists()&&filelib.isDirectory()) {
								file2=filelib;
								destDirName=parent+"\\bin\\jre\\"+file2.getName();
							}else{
								continue;
							}
						}
						txt_msg.append("正在更新文件："+file2+"\n");
						String srcDirName=file2.toString();
						System.out.println(srcDirName);
						CopyFileUtil.copyDirectory(srcDirName, destDirName, true);
						txt_msg.append("更新文件夹"+srcDirName+"成功\n");
					}
				}else{
					if (file.getName().equals("升级.exe")||file.getName().equals("升级.ini")) {
						continue;
					}
					String destFileName=parent+"\\"+file.getName();
					CopyFileUtil.copyFile(file.toString(), destFileName, true);
				}
			}
		} catch (Exception e) {
			txt_msg.append("更新时发生错误");
		}
	}
	/**
	 * 删除文件夹以及其中的文件
	 * @param dir
	 * @return
	 */
	public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

}
