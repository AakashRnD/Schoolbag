package in.ac.iitb.cse.aakash.schoolbag;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public
class MainActivity extends Activity implements SubjectsFragment.NavigationDrawerCallbacks{
	private SubjectsFragment mSubjectsFragment;
	private CharSequence     mTitle;

	@Override
	protected
	void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSubjectsFragment
		  =(SubjectsFragment)getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle=getTitle();
		// Set up the drawer.
		mSubjectsFragment.setUp(R.id.navigation_drawer,
		                        (DrawerLayout)findViewById(R.id.drawer_layout));
	}

	@Override
	public
	void onNavigationDrawerItemSelected(String subject_path){
		// update the main content by replacing fragments
		FragmentManager fragmentManager=getFragmentManager();
		fragmentManager.beginTransaction()
		               .replace(R.id.container,
		                        PlaceholderFragment.newInstance(subject_path))
		               .commit();
	}

	public
	void onSectionAttached(String subjectPath){
		mTitle=subjectPath.substring(subjectPath.lastIndexOf("/")+1);
	}

	public
	void restoreActionBar(){
		ActionBar actionBar=getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public
	boolean onCreateOptionsMenu(Menu menu){
		if(!mSubjectsFragment.isDrawerOpen()){
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main,menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item){
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id=item.getItemId();
		if(id==R.id.action_settings){
			startActivity(new Intent(this,Settings.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static
	class PlaceholderFragment extends Fragment{
		private static final String ARG_SUBJECT_PATH="arg_subject_path";
		private static       String sp              ="";

		public static
		PlaceholderFragment newInstance(String subject_path){
			PlaceholderFragment fragment=new PlaceholderFragment();
			Bundle args=new Bundle();
			args.putString(ARG_SUBJECT_PATH,subject_path);
			fragment.setArguments(args);
			sp=subject_path;
			return fragment;
		}

		public
		PlaceholderFragment(){
		}

		@Override
		public
		View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
			View rootView=inflater.inflate(R.layout.fragment_main,container,false);
			ListView listView=(ListView)rootView.findViewById(R.id.chapter_list);
			final ArrayList<String>
			  chapters
			  =getChapters(getArguments().getString(ARG_SUBJECT_PATH));
			listView.setAdapter(new ArrayAdapter<String>(getActivity().getActionBar()
			                                                      .getThemedContext(),
			                                       android.R.layout.simple_list_item_activated_1,
			                                       android.R.id.text1,
			                                       chapters));

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public
				void onItemClick(AdapterView<?> parent,View view,int position,long id){
					Intent intent=new Intent(getActivity(),ReadBook.class);
					intent.putExtra("file",sp+"/"+chapters.get(position)+".epub");
					intent.putExtra("load","external");
					startActivity(intent);
				}
			});
			return rootView;
		}

		private
		ArrayList<String> getChapters(String subject_path){
			File dir=new File(subject_path);
//			Log.d("path",Vars.SD_CARD+"/"+Vars.Path.Schoolbag+"/"+Vars.Path.Class+" "+std+"/");
			File[] files=dir.listFiles();
			//	Log.d("folder size",""+files.length);
			ArrayList<String> chapters=new ArrayList<>();
			if(files!=null) for(File f : files)
				if(f.isFile()) chapters.add(f.getName().substring(0,f.getName().length()-5));
			return chapters;
		}

		@Override
		public
		void onAttach(Activity activity){
			super.onAttach(activity);
			((MainActivity)activity).onSectionAttached(getArguments().getString(ARG_SUBJECT_PATH));
		}
	}
}
