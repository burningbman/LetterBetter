package com.cor31.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.cor31.letterbetter.GameActivity;
import com.cor31.letterbetter.R;

public class LetterButton extends Button
{
    private boolean bIsSelected, bIsBuying;
    private int[] iArrLocation;
    private GameActivity actParent;
    private Drawable drawHighlighted, drawableNormal;

    public boolean isSelected()
    {
        return bIsSelected;
    }

    public void setSelected(boolean selected)
    {
        this.bIsSelected = selected;
        if (selected)
        {
            this.setTextColor(Color.RED);
            this.setBackgroundResource(R.drawable.letter_selection3);
            //((AnimationDrawable) this.getBackground()).start();
        }
        else
        {
            this.setBackground(getResources().getDrawable(R.drawable.tile));
            this.setTextColor(Color.BLACK);
        }

    }

    public LetterButton(Context context)
    {
        super(context);
        bIsBuying = false;
    }

    public LetterButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setTextColor(Color.BLACK);
        this.setTextSize(getResources().getInteger(R.integer.tile_text_size));
        this.setBackground(getResources().getDrawable(R.drawable.tile));

        actParent = (GameActivity) context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.letter_button);
        int x = a.getInteger(0, 0);
        int y = a.getInt(1, 1);
        bIsBuying = a.getBoolean(R.styleable.letter_button_isBuyingVowel, false);
        iArrLocation = new int[] {x, y};
        a.recycle();
        /*
        gestureDetector = new GestureDetector(this.getContext(), new SingleTapConfirm());
		
		
		this.setOnTouchListener(new OnTouchListener() {			
			public boolean onTouch(View v, MotionEvent event)
			{
				LetterButton button = (LetterButton) v;
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{			
					if (button.isSwapping())
					{
						ClipData data = ClipData.newPlainText("", "");
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
						v.startDrag(data, shadowBuilder, v, 0);
					}
					else if (button.isBuyingVowel())
					{
						button.getGameActivity().onVowelClicked(v);
					}
					else if (!gestureDetector.onTouchEvent(event))
					{
						setSpelling(true);
						clicked();
						ClipData data = ClipData.newPlainText("", "");
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder();
						v.startDrag(data, shadowBuilder, v, 0);						
					}
					else
						clicked();
					return true;
				}
				else
					return false;
			}
		});*/
		
		/*
		this.setOnDragListener(new OnDragListener()
		{
			@Override
			public boolean onDrag(View v, DragEvent event)
			{
				LetterButton button = (LetterButton) v;
				if (!isSwapping() && !isSpelling())
					return false;
				
				LetterButton src = (LetterButton) event.getLocalState();
						
				switch (event.getAction())
				{
					case DragEvent.ACTION_DRAG_STARTED:
						if (isSwapping())
						{
							ClipData data = ClipData.newPlainText("", "");
							DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
							v.startDrag(data, shadowBuilder, v, 0);
						}
						break;				
					case DragEvent.ACTION_DRAG_ENTERED:
						if (isSwapping())
							button.onDragEnter();
						break;
					case DragEvent.ACTION_DRAG_EXITED:
						if (isSwapping())
							button.onDragExit();
						break;
					case DragEvent.ACTION_DRAG_LOCATION:
						if (isSpelling()  && !src.isSelected() && draggedThroughMiddle(src.getX(),
						 src.getY()))
							src.clicked();
						break;
					case DragEvent.ACTION_DROP:
						if (isSwapping())
						{
							button.onDragExit();
							src.onDragExit();
							button.swap(src);
						}
						else if (isSpelling())
							button.clicked();
						break;
					case DragEvent.ACTION_DRAG_ENDED:
						if (isSpelling())
						{
							actParent.submitWord();
							setSpelling(false);
						}
					default:
						break;
			  }
			  return true;
			}
			
		});*/

        drawHighlighted = getResources().getDrawable(R.drawable.tile_bordered);
        drawableNormal = getResources().getDrawable(R.drawable.tile_textured);
    }

    public LetterButton(Context context, AttributeSet attrs, int style)
    {
        super(context, attrs, style);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

    public int[] getLocation()
    {
        return iArrLocation;
    }

    public boolean isCorner()
    {
        return (iArrLocation[0] == 0 || iArrLocation[0] == 3) && (iArrLocation[1] == 0 ||
                iArrLocation[1] == 3);
    }

    public boolean isSwapping()
    {
        return actParent.isSwapping();
    }

    public boolean isBuyingVowel()
    {
        return bIsBuying;
    }

    public boolean isPlacingVowel()
    {
        return actParent.isPlacingVowel();
    }

    public void swap(LetterButton src)
    {
        actParent.endPowerupSwap(this, src);
    }

    public void clicked()
    {
        if (isPlacingVowel())
            actParent.endPowerupVowel(this);
        else
            actParent.letterClicked(this);
    }

    public void onDragEnter()
    {
        setBackground(drawHighlighted);
    }

    public void onDragExit()
    {
        setBackground(drawableNormal);
    }

    public GameActivity getGameActivity()
    {
        return actParent;
    }

    public boolean draggedThroughMiddle(MotionEvent event)
    {
        float x = event.getRawX();
        float y = event.getRawY();
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        float xCenter = location[0] + this.getWidth() / 2;
        float yCenter = location[1] + this.getHeight() / 2;
        float radius = this.getHeight() / 4;

        return ((xCenter - x) * (xCenter - x) + (yCenter - y) * (yCenter - y) < radius * radius);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof LetterButton)
        {
            LetterButton lb = (LetterButton) o;
            return iArrLocation[0] == lb.getLocation()[0] && iArrLocation[1] == lb.getLocation()[1];
        }
        else
            return false;
    }
}
