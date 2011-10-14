function runMandelbrot(local, maxIterations, gridSize, divisor)
persistent cpuTime
if ~exist('local', 'var')
    local = false;
end
if ~exist('maxIterations', 'var')
    maxIterations = 500;
end
if ~exist('gridSize', 'var')
    gridSize = 1000;
end
if ~exist('divisor', 'var')
    divisor = 200;
end
blockdims = [gridSize./divisor gridSize./divisor];

xlim = [-0.748766713922161, -0.748766707771757];
ylim = [ 0.123640844894862,  0.123640851045266];
% Setup
t = tic();
x = linspace( xlim(1), xlim(2), gridSize );
y = linspace( ylim(1), ylim(2), gridSize );
[xGrid,yGrid] = meshgrid( x, y );

% Calculate
if local
    count = arrayfun( @processMandelbrotElement, xGrid, yGrid, maxIterations*ones(gridSize) );
    cpuTime = toc( t );
    set( gcf, 'Position', [200 200 600 600] );
    imagesc( x, y, count );
    axis image
    colormap( [jet();flipud( jet() );0 0 0] );
    title( sprintf( 'Local Mandelbrot: %1.2fsecs', cpuTime ) );
else
    [cnt, inds] = PAarrayfun( @processMandelbrotElement, [divisor divisor], xGrid, yGrid, maxIterations );
    count = zeros(length(x),length(y));
    h=[];
    try
        while true
            [val,sub] = PAwaitAny(cnt);
            ind = sub2ind(blockdims, sub(1),sub(2));
            count(inds{ind}) = val;

            % Show
            if isempty(h)
                h=imagesc( x, y, count );
                axis image
                colormap( [jet();flipud( jet() );0 0 0] );
                title( sprintf( 'Mandelbrot with ProActive (ongoing)') );
            else
                set(h,'CData',count);
            end
            drawnow();
        end

    catch ME
        %disp(getReport(ME));
    end
    PATime = toc( t );
    if ~exist('cpuTime', 'var')
        warning('Please run the function locally first to allow speed comparison');
    end
    title( sprintf( 'Mandelbrot with ProActive: %1.2fsecs = %1.1fx faster', ...
        PATime, cpuTime/PATime ) );
    drawnow();

end
end