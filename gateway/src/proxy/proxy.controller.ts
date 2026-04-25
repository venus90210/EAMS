import { Controller, All, Req, Res, UseGuards } from '@nestjs/common';
import { Request, Response } from 'express';
import { firstValueFrom } from 'rxjs';
import { ProxyService } from './proxy.service';
import { ValidatedUser } from '../auth/jwt.strategy';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@Controller('api')
@UseGuards(JwtAuthGuard)
export class ProxyController {
  constructor(private proxyService: ProxyService) {}

  @All('*')
  async proxy(@Req() req: Request, @Res() res: Response): Promise<void> {
    const user = req.user as ValidatedUser;

    try {
      const response = await firstValueFrom(this.proxyService.forward(req, user));
      console.log(`[PROXY] ${req.method} ${req.path} -> ${response?.status}`, {
        hasData: response?.data !== undefined,
        dataType: typeof response?.data,
      });
      if (response && response.status && response.data !== undefined) {
        res.status(response.status).json(response.data);
      } else {
        res.status(500).json({ error: 'GATEWAY_ERROR', message: 'Invalid response from backend' });
      }
    } catch (error: unknown) {
      const axiosError = error as any;
      console.log(`[PROXY] ERROR ${req.method} ${req.path}:`, {
        status: axiosError?.response?.status,
        message: axiosError?.message,
      });
      const status = axiosError?.response?.status || 500;
      const data = axiosError?.response?.data || {
        error: 'GATEWAY_ERROR',
        message: axiosError?.message || 'Unknown error'
      };
      res.status(status).json(data);
    }
  }
}
