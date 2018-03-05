package view.com.youtubeviewdesign;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.pedrovgs.DraggableView;

public class VideoYoutubeViewFragment extends Fragment {

	Button id_data;


	int value;
	public VideoYoutubeViewFragment(int i) {
		// TODO Auto-generated constructor stub
		value=i;
	}
	DraggableView draggableView;
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view=inflater.inflate(R.layout.top_fragment,container,false);
		id_data= (Button) view.findViewById(R.id.id_data);
		id_data.setText(String.valueOf(value));
		return view;
	}

}
