package in.ac.iitb.cse.aakash.schoolbag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import in.ac.iitb.cse.aakash.schoolbag.util.SystemUiHider;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

public
class ReadBook extends Activity{
	private static final boolean AUTO_HIDE             =true;
	private static final int     AUTO_HIDE_DELAY_MILLIS=3000;
	private static final boolean TOGGLE_ON_CLICK       =true;
	private static final int     HIDER_FLAGS           =SystemUiHider.FLAG_HIDE_NAVIGATION;
	private SystemUiHider mSystemUiHider;
	Handler  mHideHandler =new Handler();
	Runnable mHideRunnable=new Runnable(){
		@Override
		public
		void run(){
			mSystemUiHider.hide();
		}
	};
	private String path;

	private
	void delayedHide(int delayMillis){
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable,delayMillis);
	}

	Book           book;
	WebView        wv;
	ProgressDialog loadingBookDialog;
	DrawerLayout   contentView;
	ListView       mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	public
	void onBackPressed(){
		super.onBackPressed();
	}

	@Override
	public
	void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		loadingBookDialog=new ProgressDialog(this);
		loadingBookDialog.setCancelable(false);
		loadingBookDialog.setIndeterminate(false);
		loadingBookDialog.setMessage("Loading ...");
		setContentView(R.layout.activity_read_book);
		final View controlsView=findViewById(R.id.fullscreen_content_controls);
		contentView=(DrawerLayout)findViewById(R.id.drawer_layout);
		mSystemUiHider=SystemUiHider.getInstance(this,contentView,HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener(){
			// Cached values.
			int mControlsHeight;
			int mShortAnimTime;

			@Override
//			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
			public
			void onVisibilityChange(boolean visible){
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB_MR2){
					if(mControlsHeight==0){
						mControlsHeight=controlsView.getHeight();
					}
					if(mShortAnimTime==0){
						mShortAnimTime=getResources().getInteger(android.R.integer.config_shortAnimTime);
					}
					controlsView.animate().translationY(visible?0:mControlsHeight).setDuration(mShortAnimTime);
				}
				else{
					controlsView.setVisibility(visible?View.VISIBLE:View.GONE);
				}
				if(visible&&AUTO_HIDE){
					// Schedule a hide().
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			}
		});
		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener(){
			@Override
			public
			void onClick(View view){
				if(TOGGLE_ON_CLICK){
					mSystemUiHider.toggle();
				}
				else{
					mSystemUiHider.show();
				}
			}
		});
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		mDrawerList=(ListView)findViewById(R.id.left_drawer);
		mDrawerToggle=new ActionBarDrawerToggle(this,
		                                        ReadBook.this.contentView,
		                                        R.drawable.ic_drawer,
		                                        R.string.drawer_open,
		                                        R.string.drawer_close){
			public
			void onDrawerOpened(View drawerView){
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle("Schoolbag");
			}

			public
			void onDrawerClosed(View view){
				super.onDrawerClosed(view);
				getActionBar().setTitle("Schoolbag");
			}
		};
		this.contentView.setDrawerListener(mDrawerToggle);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		path=getIntent().getStringExtra("file");
		setTitle(path.substring(path.lastIndexOf('/')+1,path.lastIndexOf('.')));
		updateContentView();
	}

	@Override
	public
	void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
		delayedHide(100);
	}

	@Override
	public
	void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public
	boolean onCreateOptionsMenu(Menu menu){
//		getMenuInflater().inflate(R.menu.read_book,menu);
		return true;
	}

	public
	boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case android.R.id.home:
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public
	List<TocEntry> getTableOfContents(){
		if(this.book==null){
			return null;
		}
		List<TocEntry> result=new ArrayList<>();
		flatten(book.getTableOfContents().getTocReferences(),result,0);
		return result;
	}

	private
	void flatten(List<TOCReference> refs,List<TocEntry> entries,int level){
		if(refs==null||refs.isEmpty()){
			return;
		}
		for(TOCReference ref : refs){
			String title="";
			for(int i=0;i<level;i++){
				title+="-";
			}
			title+=ref.getTitle();
			if(ref.getResource()!=null){
				entries.add(new TocEntry(title,ref.getCompleteHref()));
			}
			flatten(ref.getChildren(),entries,level+1);
		}
	}

	public
	void updateContentView(){
		try{
			wv=(WebView)findViewById(R.id.bookContentWeb);
			File filePath=new File(path);
			Log.d("is file present",filePath.exists()+"");
			InputStream epubInputStream=new BufferedInputStream(new FileInputStream(filePath));
			book=(new EpubReader()).readEpub(epubInputStream);
			DownloadResource(path.substring(0,path.lastIndexOf("."))+"/");
			final List<TocEntry> tocList=getTableOfContents();
			if(tocList==null||tocList.isEmpty()){
				return;
			}
			final String[] items=new String[tocList.size()];
			for(int i=0;i<items.length;i++){
				items[i]=tocList.get(i).getTitle();
				Log.d("Toc Item",items[i]);
			}
			ArrayAdapter<String> adapter=new ArrayAdapter<>(getApplicationContext(),
			                                                android.R.layout.simple_list_item_1,
			                                                android.R.id.text1,
			                                                items);
			mDrawerList.setAdapter(adapter);
			mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public
				void onItemClick(AdapterView<?> parent,View view,int position,long id){
					new LoadEpubChapter().execute(position);
					contentView.closeDrawer(mDrawerList);
				}
			});
			WebSettings settings=wv.getSettings();
			settings.setJavaScriptEnabled(true);
			settings.setBuiltInZoomControls(true);
			new LoadEpubChapter().execute(0);
		}
		catch(IOException e){
			Log.e("epublib",e.getMessage());
		}
	}

	private
	void DownloadResource(String directory){
		try{
//			File dir=new File(directory);
//			File[] existing=dir.listFiles();
//			if(existing!=null) for(File f : existing)
//				if(f.isDirectory()){
//					if(f.getName().contains("Images")) f.delete();
//					if(f.getName().contains("images")) f.delete();
//					if(f.getName().contains("css")) f.delete();
//					if(f.getName().contains("font")) f.delete();
//					if(f.getName().contains("Fonts")) f.delete();
//					if(f.getName().contains("Styles")) f.delete();
//				}
			Iterator<Resource> itr=book.getResources().getAll().iterator();
			while(itr.hasNext()){
				Resource rs=itr.next();
				if((rs.getMediaType()==MediatypeService.JPG)
				   ||(rs.getMediaType()==MediatypeService.PNG)
				   ||(rs.getMediaType()==MediatypeService.GIF)
				   ||rs.getMediaType()==MediatypeService.CSS
				   ||rs.getMediaType()==MediatypeService.TTF
				   ||rs.getMediaType()==MediatypeService.OPENTYPE
				   ||rs.getMediaType()==MediatypeService.WOFF){
					String res=rs.getHref();
					File paths;
					if(res.contains("Images/")){
						paths=new File(directory+"/Images");
						paths.mkdirs();
					}
					if(res.contains("images/")){
						paths=new File(directory+"/images");
						paths.mkdirs();
					}
					if(res.contains("css/")){
						paths=new File(directory+"/css");
						paths.mkdirs();
					}
					if(res.contains("font/")){
						paths=new File(directory+"/font");
						paths.mkdirs();
					}
					if(res.contains("Fonts/")){
						paths=new File(directory+"/Fonts");
						paths.mkdirs();
					}
					if(res.contains("Styles/")){
						paths=new File(directory+"/Styles");
						paths.mkdirs();
					}
					File oppath1=new File(directory+"/"+res);
					if(!oppath1.exists()){
						oppath1.createNewFile();
						FileOutputStream fos1=new FileOutputStream(oppath1);
						fos1.write(rs.getData());
						fos1.close();
						oppath1.deleteOnExit();
					}
				}
			}
		}
		catch(IOException e){
			Log.e("error downloading resource",e.getMessage());
		}
	}

	public static
	class TocEntry{
		private String title;
		private String href;

		public
		TocEntry(String title,String href){
			this.title=title;
			this.href=href;
		}

		public
		String getHref(){
			return href;
		}

		public
		String getTitle(){
			return title;
		}
	}

	class LoadEpubChapter extends AsyncTask<Integer,Void,Void>{
		String html=null, line;
		StringBuilder string=new StringBuilder();
		Resource r;

		@Override
		protected
		Void doInBackground(Integer... args){
			r=book.getSpine().getResource(args[0]);
			try{
				InputStream is=r.getInputStream();
				BufferedReader reader=new BufferedReader(new InputStreamReader(is));
				try{
					while((line=reader.readLine())!=null){
						html=string.append(line+"\n").toString();
					}
				}
				catch(IOException e){
				}
			}
			catch(IOException e){
			}
			//Log.d("ChapterContent", html);
			runOnUiThread(new Runnable(){
				@Override
				public
				void run(){
					wv.loadDataWithBaseURL("file://"+path.substring(0,path.lastIndexOf("."))+"/Text/",
					                       html,
					                       "text/html",
					                       "UTF-8",
					                       null);
				}
			});
			return null;
		}

		@Override
		protected
		void onPreExecute(){
			loadingBookDialog.show();
			super.onPreExecute();
		}

		@Override
		protected
		void onPostExecute(Void result){
			loadingBookDialog.dismiss();
			super.onPostExecute(result);
		}
	}
}
