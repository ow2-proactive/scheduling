% PAarrayfun apply a remote function call to each element of an array.
%
% Syntax
%       
%       >> resultarray = PAarrayfun(func, blocks, array1, array2, ...);
%       >> [resultarray, indices] = PAarrayfun(func, blocks, array1, array2, ...);
%       
%
% Inputs
%
%       func - a matlab function handle
%
%       blocks - a vector describing the size of blocks to create. Its
%       length must correspond to the number of dimensions of arrays
%       parameters. Example: [20 20] will create blocks of size 20x20.
%
%       arrayk - an array whose elements will be given to parameter k of function func, each arrayk must either be of
%       the same size or a scalar. In case blocks are used, func will
%       receive blocks of size specified by the blocks parameter,
%       instead of a scalar value.
%
%
% Outputs
%
%       resultarray = an array of PAResult objects, the size of the
%       result array will be the same as arrayk parameters. In case
%       blocks are used, the result array's size will be the parameters
%       size divided by the blocks size.
%
%       indices = in case blocks are used, indices is a cell array which
%       will contain for each block, the indices of the original block
%       taken from the parameter arrayk. The indices use linear indexing.
%       So is the indexing used to indentify the block in the indices cell array.
%       Example {[1 2 4 5], [3 4 7 8]} means that block 1 correspond to the
%       block [1 2 4 5] of arrayk and block 2 corresponds to the block [3 4
%       7 8] of arrayk.
%       
%
% Description
%
%       PAarrayfun apply a remote function call to each element of an
%       array. It realises inside a call to PAsolve, so refer to the
%       description of PAsolve for behavior and remarks. 
%       The parameter array can be divided into blocks of equal size. This should be done
%       systematically when trying to process big matrices. Avoiding doing that can result
%       in huge overheads and Out of Memory issues.              
%
%       PATasks configuration such as Input/Ouput Files cannot be used with 
%       PAarrayfun.
%
%       PAwaitFor, PAisAwaited or PAwaitAny can be used on the result array
%       similarly as with PAsolve.
%       
%       In the Example folder is a complete example of using PAarrayfun
%       applied to a Mandelbrot fractal calculation.
%   
%
% See also
%       PAsolve, PAResult, PAResult/PAwaitFor,
%       PAResult/PAwaitAny, PAResult/PAisAwaited
%


% /*
%   * ################################################################
%   *
%   * ProActive Parallel Suite(TM): The Java(TM) library for
%   *    Parallel, Distributed, Multi-Core Computing for
%   *    Enterprise Grids & Clouds
%   *
%   * Copyright (C) 1997-2011 INRIA/University of
%   *                 Nice-Sophia Antipolis/ActiveEon
%   * Contact: proactive@ow2.org or contact@activeeon.com
%   *
%   * This library is free software; you can redistribute it and/or
%   * modify it under the terms of the GNU Affero General Public License
%   * as published by the Free Software Foundation; version 3 of
%   * the License.
%   *
%   * This library is distributed in the hope that it will be useful,
%   * but WITHOUT ANY WARRANTY; without even the implied warranty of
%   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
%   * Affero General Public License for more details.
%   *
%   * You should have received a copy of the GNU Affero General Public License
%   * along with this library; if not, write to the Free Software
%   * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
%   * USA
%   *
%   * If needed, contact us to obtain a release under GPL Version 2 or 3
%   * or a different license than the AGPL.
%   *
%   *  Initial developer(s):               The ProActive Team
%   *                        http://proactive.inria.fr/team_members.htm
%   *  Contributor(s):
%   *
%   * ################################################################
%   * $$PROACTIVE_INITIAL_DEV$$
%   */
function varargout = PAarrayfun(func, steps, varargin)
    n=-1;
    shp=[];
    for j=1:length(varargin)
        n2 = numel(varargin{j});
        if n2 == 1
            continue;
        elseif n == -1
            n = n2;
            shp = varargin{j};
        elseif n2 ~= n           
            error(['Dimension mismatch, expected ' num2str(n) ', received ' num2str(n2)]); 
        end            
    end
    if n == -1
        n=1;
    end 
    if ~isempty(steps)
        szM = size(shp);
        szB = szM./steps;
        n=prod(szB);
        t=PATask(1,n);
        t(1:n).Func=func;
        for i=1:n
            p=cell(1,length(varargin));
            for j=1:length(varargin)
                n2=numel(varargin{j});
                if n2 == 1
                    p{j}=varargin{j};
                else
                    p{j}=getBlock(varargin{j}, steps, i);
                end
            end
            t(i).Params = p;
        end
        disp(t);
        r=PAsolve(t);
        results=reshape(r,szB);
    else
        t=PATask(1,n);
        t(1:n).Func=func;
        for i=1:n
            p=cell(1,length(varargin));
            for j=1:length(varargin)
                n2=numel(varargin{j});
                if n2 == 1
                    p{j}=varargin{j};
                else
                    p{j}=varargin{j}(i);
                end
            end
            t(i).Params = p;
        end
        r=PAsolve(t);
        results=reshape(r,size(shp));
    end
    varargout{1} = results;
    if nargout > 1        
        if isempty(steps)
            error('Unexpected second output with steps parameter empty.');
        end
        szM = size(shp);
        szB = szM./steps;
        inds = cell(1,n);
        for j=1:n
            [BI{1:ndims(shp)}] = ind2sub(szB,j);
            RI = cell(1,ndims(shp));
            for i=1:ndims(shp)
                RI{i} = (1+(BI{i}-1)*steps(i)):BI{i}*steps(i);
            end
            [GI{1:ndims(shp)}] = ndgrid(RI{:});
            GI = cellfun(@(x)x(:),GI, 'UniformOutput', false);
            inds{j} = sub2ind(szM,GI{:}); 
        end
        varargout{2} = inds;
    end
end

function block=getBlock(matrix, steps, ind)
    szM = size(matrix);
    if any(mod(szM,steps))
        error('Steps must be divisors of matrix size');
    end
    szB = szM./steps;
    [BI{1:ndims(matrix)}] = ind2sub(szB,ind);
    RI = cell(1,ndims(matrix));
    for i=1:ndims(matrix)
        RI{i} = (1+(BI{i}-1)*steps(i)):BI{i}*steps(i);
    end
    [GI{1:ndims(matrix)}] = ndgrid(RI{:});
    GI = cellfun(@(x)x(:),GI, 'UniformOutput', false);
    si = sub2ind(szM,GI{:});
    block=reshape(matrix(si),steps);    
end