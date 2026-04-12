import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  BadRequestException,
  NotFoundException,
  ForbiddenException,
  ConflictException,
  InternalServerErrorException,
} from '@nestjs/common';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AxiosError } from 'axios';

@Injectable()
export class ErrorNormalizerInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next): Observable<any> {
    return next.handle().pipe(
      catchError((error) => {
        if (error instanceof AxiosError) {
          const status = error.response?.status || 500;
          const data = error.response?.data as any;

          const normalizedError = {
            error: data?.error || data?.errorCode || 'UNKNOWN_ERROR',
            message: data?.message || error.message || 'An error occurred',
          };

          switch (status) {
            case 400:
              return throwError(() => new BadRequestException(normalizedError));
            case 404:
              return throwError(() => new NotFoundException(normalizedError));
            case 403:
              return throwError(() => new ForbiddenException(normalizedError));
            case 409:
              return throwError(() => new ConflictException(normalizedError));
            default:
              return throwError(() => new InternalServerErrorException(normalizedError));
          }
        }

        return throwError(() => error);
      }),
    );
  }
}
